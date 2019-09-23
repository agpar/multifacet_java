from clustering.cluster_tools import *


def cluster(dists: np.array, k, iters):
    old_clusteroids = initial_clusteroids(dists, k, strategy='central')
    clusters = assign_clusters(dists, old_clusteroids)
    print(f"Cluster score: {eval(dists, clusters_to_labels(dists, clusters))}")
    for i in range(iters - 1):
        new_clusteroids = [0 for i in range(k)]
        for i in range(k):
            new_clusteroids[i] = next_clusteroid(dists, clusters[i], strategy="mse")
        #if sorted(old_clusteroids) == sorted(new_clusteroids):
        #    print("Converged.")
        #    break
        clusters = assign_clusters(dists, new_clusteroids)
        print(f"Cluster score: {average_intra_clust_distance(dists, clusters)}")
        print(f"Cluster score: {eval(dists, clusters_to_labels(dists, clusters))}")

    return clusters_to_labels(dists, clusters)


def assign_clusters(dists, clusteroids):
    clusters = {i: set() for i in range(len(clusteroids))}
    for i, point_dists in enumerate(dists):
        clusters[clusteroids.index(nearest_clusteroid(i, clusteroids))].add(i)
    return clusters


def initial_clusteroids(dists, k, strategy='central'):
    """Chose a set of k initial clusteroids

    :param dists: A N*N dist martrix.
    :param k: The number of clusteroids to choose.
    :param strategy: A strategy in ('central', 'random')
    :return: The indexes of k clusteroids.
    """
    if strategy == 'random':
        return dists[list(np.random.choice(np.array(list(range(len(dists)))), k))]
    elif strategy == 'central':
        means = sorted_with_idx(mean_dists(dists))
        clusteroids = []
        for i in range(k):
            clusteroid = next_idx_to_cluster(means, clusteroids)
            clusteroids.append(clusteroid)
        return dists[clusteroids]



