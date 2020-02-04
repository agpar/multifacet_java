import sys
import numpy as np
from multiprocessing.pool import Pool
from multiprocessing.shared_memory import SharedMemory


def _clust_and_eval(cluster_fn, mem_tup, k, iters, eval_fn):
    # The dists are loaded off disk in each proc as trying to pickle and unpickle the dists
    # is slow and causes a huge memory spike.
    try:
        print(f"Starting cluster task for {k}")
        # Attach to shared memory.
        name, shape, dtype = mem_tup
        smem = SharedMemory(create=False, name=name)
        dists = np.ndarray(shape, dtype=dtype, buffer=smem.buf)

        clusts = cluster_fn(dists, k, iters)
        print(f"Finishing cluster task for {k}")
        return k, eval_fn(dists, clusts)
    except Exception as e:
        print(f"Clustering failed for {k} clusters. Exception below.", file=sys.stderr)
        print(e, file=sys.stderr)
        return k, (-1, -1)
    finally:
        smem.close()


def open_memory(dists_path):
    loaded_data = np.load(dists_path)
    mem = SharedMemory(create=True, size=loaded_data.nbytes)
    shared_data = np.ndarray(loaded_data.shape, loaded_data.dtype, buffer=mem.buf)
    shared_data[:] = loaded_data[:]
    del loaded_data
    mem_info = (mem.name, shared_data.shape, shared_data.dtype)
    return shared_data, mem_info

def choose_k(cluster_fn, dists_path, k_range, iters, eval_fn, num_procs=16):
    """Evaluate multiple values of n using multiprocessing.

    :param dists_path: The realpath of saved numpy dists to load
    :param range: The range of K's to try (an iterable)
    :param eval_fn: f : (dists, clusters) -> real which scores the clusters
    :return: A list of results based on k and eval function
    """

    try:
        shared_data, mem_info = open_memory(dists_path)
        pool = Pool(num_procs)
        results = []
        for k in k_range:
            args = (cluster_fn, mem_info, k, iters, eval_fn)
            results.append(pool.apply_async(_clust_and_eval, args=args))
    finally:
        pool.close()
        pool.join()
        mem.close()
        mem.unlink()

    final_results = []
    for res in results:
        final_results.append(res.get())

    final_results.sort(key=lambda x: x[1])
    return list(reversed(final_results))
