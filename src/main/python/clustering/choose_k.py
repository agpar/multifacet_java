import numpy as np
from multiprocessing.pool import Pool
from clustering.cluster_tools import clusters_to_labels
from sklearn.metrics import silhouette_score


def eval_silouette(dists, clusters):
    return silhouette_score(dists, clusters_to_labels(dists, clusters), metric='precomputed')


def _clust_and_eval(cluster_fn, dists_path, k, iters, eval_fn):
    # The dists are loaded off disk in each proc as trying to pickle and unpickle the dists
    # is slow and causes a huge memory spike.
    dists = np.load(dists_path)
    clusts = cluster_fn(dists, k, iters)
    return k, eval_fn(dists, clusts)


def choose_k(cluster_fn, dists_path, k_range, iters, eval_fn, num_procs=14):
    """Evaluate multiple values of n using multiprocessing.

    :param dists_path: The realpath of saved numpy dists to load
    :param range: The range of K's to try (an iterable)
    :param eval_fn: f : (dists, clusters) -> real which scores the clusters
    :return: A list of results based on k and eval function
    """
    pool = Pool(num_procs)
    results = []
    for k in k_range:
        args = (cluster_fn, dists_path, k, iters, eval_fn)
        results.append(pool.apply_async(_clust_and_eval, args=args))
    pool.close()
    final_results = []
    for res in results:
        final_results.append(res.get())

    final_results.sort(key=lambda x: -x[1])
    return final_results
