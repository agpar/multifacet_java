from surprise.prediction_algorithms import KNNWithMeans
import csv
import numpy as np
from scipy.sparse import coo_matrix
from surprise import Reader, Dataset
from surprise. model_selection import PredefinedKFold
from surprise import accuracy
from tqdm import tqdm
import os


class MauroU2UCF(KNNWithMeans):
    def __init__(self, beta=0.1, trust_mat=None, **kwargs):
        super().__init__(**kwargs)
        self.beta = beta
        self.trust_mat = trust_mat

    def compute_similarities(self):
        sims = super().compute_similarities()
        print("Applying trust to similarities")

        # Assert inner ids are equiv to outer ids.
        for i in range(sims.shape[0]):
            assert(i == int(self.trainset.to_raw_uid(i)))

        for i in tqdm(range(sims.shape[0])):
            trust_row = self.trust_mat.get_row(i)
            non_empty_indices = trust_row.indices
            adjusted_sims = []
            for j in non_empty_indices:
                adjusted_sims.append(self.beta * sims[i][j] + (1 - self.beta) * self.trust_mat.get(i, j))
            sims[i] = sims[i] * self.beta
            sims[i][i] = 1
            for k, j in enumerate(non_empty_indices):
                sims[i][j] = adjusted_sims[k]

        return sims


class SparseTrustMatrix:
    def __init__(self, path):
        self.path = path
        self.mat = self._load_trust_mat(path)

    def _load_trust_mat(self, path):
        rows = []
        cols = []
        vals = []

        with open(path, 'r') as f:
            reader = csv.reader(f, delimiter=' ')
            for r, c, v in reader:
                rows.append(int(r))
                cols.append(int(c))
                vals.append(float(v))
        return coo_matrix((np.array(vals), (np.array(rows), np.array(cols)))).tocsr()

    def get(self, i, j):
        data = self.mat.getrow(i).getcol(j)
        if data.size > 0:
            return data.data[0]
        else:
            return 0

    def get_row(self, i):
        return self.mat.getrow(i)


def evaluate_links(experiment_dir, trust_file):
    trust_path = os.path.join(experiment_dir, trust_file)
    trust_mat = SparseTrustMatrix(trust_path)

    rating_train_path = os.path.join(experiment_dir, 'ratings_train.txt')
    rating_test_path = os.path.join(experiment_dir, 'ratings_test.txt')
    ratings_reader = Reader(line_format="user item rating", sep=' ')
    dataset = Dataset.load_from_folds([(rating_train_path, rating_test_path)], ratings_reader)
    pkf = PredefinedKFold()
    trainset, testset = list(pkf.split(dataset))[0]

    algo = MauroU2UCF(beta=0.1, trust_mat=trust_mat, k=50, sim_options={'name':'pearson', 'min_support':3} )
    fitted_algo = algo.fit(trainset)
    predictions = fitted_algo.test(testset)
    return {
        "MAE": accuracy.mae(predictions),
        "RMSE": accuracy.rmse(predictions),
        "predictions": predictions
    }