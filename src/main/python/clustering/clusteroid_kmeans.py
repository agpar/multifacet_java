from clustering.cluster_tools import *


def cluster(dists: np.array, k, iters):
    init_clusteroids = initial_clusteroids(dists, k, strategy='central')
    clusters = assign_clusters(dists, init_clusteroids)
    for i in range(iters - 1):
        new_clusteroids = choose_next_clusteroids(dists, k, clusters, "cluster-avg")
        #if sorted(old_clusteroids) == sorted(new_clusteroids):
        #    print("Converged.")
        #    break
        clusters = assign_clusters(dists, new_clusteroids)
        print(f"{i}: Average intra cluster dist: {average_intra_clust_distance(dists, clusters)}")
        print(f"{i}: Silo score: {eval(dists, clusters_to_labels(dists, clusters))}")

    return clusters_to_labels(dists, clusters)


def assign_clusters(dists, clusteroids):
    clusters = {i: set() for i in range(len(clusteroids))}
    for i, point_dists in enumerate(dists):
        clusters[nearest_clusteroid_index(i, clusteroids)].add(i)
    return clusters


def initial_clusteroids(dists, k, strategy='central'):
    """Chose a set of k initial clusteroids, returning a K*N dist matrix.

    :param dists: A N*N dist martrix.
    :param k: The number of clusteroids to choose.
    :param strategy: A strategy in ('central', 'random')
    :return: A K*N dist matrix, where each row is the clusteroid dist array.
    """
    if strategy == 'random':
        return dists[list(np.random.choice(dists.shape[0], k))]
    elif strategy == 'central':
        means = sorted_with_idx(mean_dists(dists))
        clusteroids = []
        for i in range(k):
            clusteroid = next_idx_to_cluster(means, clusteroids)
            clusteroids.append(clusteroid)
        return dists[clusteroids]


def choose_next_clusteroids(dists, k, clusters, strategy="sum"):
    """Return k distance arrays corresponding to new clusteroids.

    :param dists: The N*N dist matrix
    :param k: The number of clusteroids to consider.
    :param clusters: The current dict of int -> set() clusters.
    :param strategy: A string in "sum", "avg", "mse", "cluster-avg"
    :return: A list of new clusteroid distances to use.
    """
    new_clusteroids = np.empty((k, dists.shape[0]))
    if strategy == "cluster-avg":
        calculator = AvgClusterDistCalculator(dists, clusters)
        for i in range(k):
            new_clusteroids[i] = np.array(calculator.sim_clusteroid(i))
    else:
        for i in range(k):
            new_clusteroids[i] = np.array(_next_clusteroid(dists, clusters[i], strategy="mse"))
    return new_clusteroids


def _next_clusteroid(dist_matrix, cluster_idxs, strategy="sum"):
    """Return the distance array of the next clusteroid.

    :param dist_matrix: A N * N symmetric distance matrix
    :param cluster_idxs: The row indexes of the cluster to find a clusteroid for.
    :param strategy: One of "sum", "avg" or "mse".
    :return: The length N distance array of the next clusteroid.
    """
    idx_list = sorted(list(cluster_idxs))
    intra_cluster_dists = np.array([d[idx_list] for d in dist_matrix[idx_list]])
    if strategy == "sum":
        aggregated_dists = np.sum(intra_cluster_dists, axis=0)
    elif strategy == "avg":
        aggregated_dists = np.mean(intra_cluster_dists, axis=0)
    elif strategy == "mse":
        aggregated_dists = np.mean(np.square(intra_cluster_dists), axis=0)
    else:
        raise ValueError("strategy must be in ('sum', 'avg', 'mse')")

    min_dist = np.min(aggregated_dists)
    return dist_matrix[np.nonzero(aggregated_dists == min_dist)[0][0]]


def nearest_clusteroid_index(idx, clusteroid_arrs):
    """Return the index of the clusteroid closest to idx

    :param dist_array: A 1D array of distances.
    :param clusteroid_idxs: A collection of clusteroid indexes
    :return: The index of the nearest clusteroid.
    """
    clusteroid_dists = clusteroid_arrs[:, idx]
    min_dist_val = np.min(clusteroid_dists)
    min_dist_idxs = np.nonzero(clusteroid_dists == min_dist_val)[0]
    return np.random.choice(min_dist_idxs, 1)[0]
