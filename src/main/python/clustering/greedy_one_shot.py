from clustering.cluster_tools import *


def cluster_greedy(dists: np.array, clust_size):
    return cluster(dists, clust_size, closest_points_to_center)


def cluster_avg(dists: np.array, clust_size):
    return cluster(dists, clust_size, closest_points_average)


def cluster(dists: np.array, clust_size, cluster_builder):
    means = sorted_with_idx(mean_dists(dists))
    clusters = assign_clusters(dists, means, clust_size, cluster_builder)
    print(f"Cluster score: {eval(dists, clusters_to_labels(dists, clusters))}")
    return clusters_to_labels(dists, clusters)


def assign_clusters(dists, means, clust_size, cluster_builder):
    already_clustered = set()
    clusters = {}
    cluster_num = 0
    while len(dists) - len(already_clustered) > clust_size:
        next_center_idx = next_idx_to_cluster(means, already_clustered)
        cluster = cluster_builder(next_center_idx, dists, clust_size, already_clustered)
        already_clustered = already_clustered.union(cluster)
        clusters[cluster_num] = cluster
        cluster_num += 1

    last_cluster = set([idx for idx, _ in enumerate(dists) if idx not in already_clustered])
    clusters[cluster_num] = last_cluster
    return clusters


def closest_points_to_center(center_idx, dists, n, already_clustered):
    cluster = set()
    cluster.add(center_idx)
    center_dists = dists[center_idx]
    for idx, dist in sorted_with_idx(center_dists):
        if idx not in already_clustered:
            cluster.add(idx)
        if len(cluster) >= n:
            break
    return cluster


def closest_points_average(center_idx, dists, n, already_clustered):
    cluster = set()
    cluster.add(center_idx)
    local_already_clustered = already_clustered.union([center_idx])
    while len(cluster) < n:
        if len(cluster) == 1:
            cluster_dists = dists[list(cluster)][0]
        else:
            cluster_dists = np.mean(dists[list(cluster)], axis=0)
        next_item = next(idx for idx, mean in sorted_with_idx(cluster_dists) if idx not in local_already_clustered)
        local_already_clustered.add(next_item)
        cluster.add(next_item)
    return cluster
