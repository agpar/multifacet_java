import sys
import numpy as np
from multiprocessing.pool import Pool


def _clust_and_eval(cluster_fn, dists_path, k, iters, eval_fn):
    # The dists are loaded off disk in each proc as trying to pickle and unpickle the dists
    # is slow and causes a huge memory spike.
    try:
        print(f"Starting cluster task for {k}")
        dists = np.load(dists_path)
        clusts = cluster_fn(dists, k, iters)
        print(f"Finishing cluster task for {k}")
        return k, eval_fn(dists, clusts)
    except Exception as e:
        print(f"Clustering failed for {k} clusters. Exception below.", file=sys.stderr)
        print(e, file=sys.stderr)
        return k, (-1, -1)


def choose_k(cluster_fn, dists_path, k_range, iters, eval_fn, num_procs=8):
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
    pool.join()

    final_results = []
    for res in results:
        final_results.append(res.get())

    final_results.sort(key=lambda x: x[1])
    return list(reversed(final_results))
