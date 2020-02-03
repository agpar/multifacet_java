from clustering.cluster_tools import *


def cluster(dists: np.array, k, iters, save_iter_scores=False):
    init_clusteroids = choose_initial_clusteroids(dists, k, strategy='partition')
    clusters = assign_points_to_clusters(dists, init_clusteroids)
    scores = []
    for i in range(iters - 1):
        new_clusteroids = choose_next_clusteroids(dists, k, clusters, "cluster-avg")
        clusters = assign_points_to_clusters(dists, new_clusteroids)
        if save_iter_scores:
            scores.append(eval_both(dists, clusters_to_labels(dists, clusters)))

    if save_iter_scores:
        print("Iter scores:\n")
        print(scores)
    return clusters_to_labels(dists, clusters)


def assign_points_to_clusters(dists, clusteroids):
    """Assign each point to the cluster with the closest mean.

    :param dists: A N*N dist matrix.
    :param clusteroids: A K*N matrix where ij is dist from i'th clusteroid to j'th point.
    :return: A dict of int -> set(int) designating cluster assignments.
    """
    clusters = {i: set() for i in range(len(clusteroids))}
    for i, point_dists in enumerate(dists):
        clusters[nearest_clusteroid_index(i, clusteroids)].add(i)
    return clusters


def choose_initial_clusteroids(dists, k, strategy='central'):
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
    elif strategy == 'partition':
        buckets = {i: set() for i in range(k)}
        for i in range(dists.shape[0]):
            np.random.choice(list(buckets.values())).add(i)

        return AvgClusterDistCalculator(dists, buckets).avg_dist_psuedo_means()

    elif strategy == 'k++':
        clusteroids = []
        choices = set()

        choice = np.random.choice(dists.shape[0], 1)[0]
        choices.add(k)
        clusteroids.append(dists[choice])
        while len(clusteroids) < k:
            clusteroid_dists = np.array(clusteroids)
            clusteroid_mins = np.min(clusteroid_dists, axis=0)
            print(sum(clusteroid_mins))

            normalized = clusteroid_mins / np.sum(clusteroid_mins)
            # Chose a point not yet seen.
            choice = np.random.choice(dists.shape[0], 1, p=normalized)[0]
            while choice in choices:
                choice = np.random.choice(dists.shape[0], 1, p=normalized)[0]

            choices.add(choice)
            clusteroids.append(dists[choice])
        return np.array(clusteroids)


def choose_next_clusteroids(dists, k, clusters, strategy="sum"):
    """Return k distance arrays corresponding to new clusteroids.

    :param dists: The N*N dist matrix
    :param k: The number of clusteroids to consider.
    :param clusters: The current dict of int -> set() clusters.
    :param strategy: A string in "sum", "avg", "mse", "cluster-avg"
    :return: A list of new clusteroid distances to use.
    """
    if strategy == "cluster-avg":
        calculator = AvgClusterDistCalculator(dists, clusters)
        return calculator.avg_dist_psuedo_means()
    else:
        new_clusteroids = np.empty((k, dists.shape[0]))
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


def nearest_clusteroid_index(idx, clusteroids):
    """Return the index of the nearest clusteroid.

    :param idx: The point being clustered.
    :param clusteroids: A K*N clusteroid to point dist matrix.
    :return: The index of the closest clusteroid, breaking ties deterministically.
    """

    clusteroid_dists = clusteroids[:, idx]
    min_dist_val = np.nanmin(clusteroid_dists)
    min_dist_idxs = np.nonzero(clusteroid_dists == min_dist_val)[0]
    if not min_dist_idxs.any():
        return np.random.choice(clusteroids.shape[0])
    return np.nanmin(min_dist_idxs)
