import os
from multiprocessing.pool import Pool

from surprise import accuracy

from machine_learning.rec_surprise import SparseTrustMatrix, load_precomputed_data, precompute_data, \
    dump_precomputed_data, MauroU2UCF


def evaluate_links(experiment_dir="", trust_file="", beta=0.1, k=50):
    trust_path = os.path.join(experiment_dir, trust_file)
    trust_mat = SparseTrustMatrix(trust_path)

    try:
        trainset, testset, sims = load_precomputed_data(experiment_dir)
    except:
        print("Failed to load dumped data. Regenerating.")
        trainset, testset, sims = precompute_data(experiment_dir)
        dump_precomputed_data(experiment_dir, trainset, testset, sims)

    algo = MauroU2UCF(beta=beta, trust_mat=trust_mat, k=k, sims=sims)
    fitted_algo = algo.fit(trainset)
    predictions = fitted_algo.test(testset)
    return {
        "MAE": accuracy.mae(predictions),
        "RMSE": accuracy.rmse(predictions),
        "predictions": predictions
    }


class Experiment:
    def __init__(self, experiment_dir, trust_file, beta, k):
        self.experiment_dir = experiment_dir
        self.trust_file = trust_file
        self.beta = beta
        self.k = k

    @property
    def name(self):
        return "_".join(map(str, (self.trust_file, self.beta, self.k)))

    def to_dict(self):
        return {
            'experiment_dir': self.experiment_dir,
            'trust_file': self.trust_file,
            'beta': self.beta,
            'k': self.k
        }


def evaluate_all_links(experiments, **kwargs):
    # Assert all files exist:
    for exp in experiments:
        full_path = os.path.join(exp.experiment_dir, exp.trust_file)
        if not os.path.exists(full_path):
            raise Exception(f"Prediction file does not exist: {full_path}")

    pool = Pool()
    results = []
    for exp in experiments:
        results.append((exp, pool.apply_async(evaluate_links, kwds=exp.to_dict())))

    pool.close()
    all_results = []
    for exp_name, res in results:
        exp_results = res.get()
        all_results.append((exp_name, exp_results))
    pool.join()

    return all_results

