from surprise.prediction_algorithms import KNNWithMeans
import csv
import numpy as np
from scipy.sparse import coo_matrix
from surprise import Reader, Dataset
from surprise. model_selection import PredefinedKFold
from surprise.similarities import pearson
import os
import pickle

TRAIN_FILE = 'surprise_train.pickle'
TEST_FILE = 'surprise_test.pickle'
SIM_FILE = 'surprise_sims.npy'


class MauroU2UCF(KNNWithMeans):
    def __init__(self, beta=0.1, trust_mat=None, sims=None, **kwargs):
        super().__init__(**kwargs)
        self.beta = beta
        self.trust_mat = trust_mat
        self.precomputed_sims = sims

    def compute_similarities(self):
        if self.precomputed_sims is not None:
            sims = self.precomputed_sims
        else:
            sims = super().compute_similarities()

        # Assert inner ids are equiv to outer ids.
        for i in range(sims.shape[0]):
            assert(i == int(self.trainset.to_raw_uid(i)))

        for i in range(sims.shape[0]):
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


def precompute_data(experiment_dir):
    rating_train_path = os.path.join(experiment_dir, 'ratings_train.txt')
    rating_test_path = os.path.join(experiment_dir, 'ratings_test.txt')
    ratings_reader = Reader(line_format="user item rating", sep=' ')
    dataset = Dataset.load_from_folds([(rating_train_path, rating_test_path)], ratings_reader)
    pkf = PredefinedKFold()
    trainset, testset = list(pkf.split(dataset))[0]

    n_x, yr = trainset.n_users, trainset.ir
    min_support = 1
    args = [n_x, yr, min_support]
    sims = pearson(*args).astype(np.float32)
    return trainset, testset, sims


def dump_precomputed_data(experiment_dir, train, test, sims):
    with open(os.path.join(experiment_dir, TRAIN_FILE), 'wb') as f:
        pickle.dump(train, f)

    with open(os.path.join(experiment_dir, TEST_FILE), 'wb') as f:
        pickle.dump(test, f)

    np.save(os.path.join(experiment_dir, SIM_FILE), sims)


def load_precomputed_data(experiment_dir):
    with open(os.path.join(experiment_dir, TRAIN_FILE), 'rb') as f:
        train = pickle.load(f)
    with open(os.path.join(experiment_dir, TEST_FILE), 'rb') as f:
        test = pickle.load(f)
    sims = np.load(os.path.join(experiment_dir, SIM_FILE))
    return train, test, sims

