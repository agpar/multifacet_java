import numpy as np


def cluster(dists: np.array, k, iters):
    return np.random.randint(0, k, dists.shape[0]).tolist()
