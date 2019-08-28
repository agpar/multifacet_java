import numpy as np


def iter_cluster_greedy(dists: np.array, clust_size):
    return iter_cluster(dists, clust_size, closest_points_to_center)


def iter_cluster_avg(dists: np.array, clust_size):
    return iter_cluster(dists, clust_size, closest_points_average)


def iter_cluster(dists: np.array, clust_size, cluster_builder):
    already_clustered = set()
    clusters = {}
    cluster_num = 0
    means = sorted_with_idx(mean_dists(dists))
    while len(dists) - len(already_clustered) > clust_size:
        next_center_idx = next_idx_to_cluster(means, already_clustered)
        cluster = cluster_builder(next_center_idx, dists, clust_size, already_clustered)
        already_clustered = already_clustered.union(cluster)
        clusters[cluster_num] = cluster
        cluster_num += 1

    last_cluster = set([idx for idx, _ in enumerate(dists) if idx not in already_clustered])
    clusters[cluster_num] = last_cluster
    cluster_labels = np.full(len(dists), -1)
    for key, vals in clusters.items():
        for val in vals:
            cluster_labels[val] = key
    return cluster_labels


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


def next_idx_to_cluster(sorted_means_with_idx, already_clustered):
    for idx, mean in sorted_means_with_idx:
        if idx not in already_clustered:
            return idx
    raise Exception("Nothing left to cluster!")


def sorted_with_idx(vals):
    idx_vals = list(enumerate(vals))
    idx_vals.sort(key=lambda tup: tup[1])
    return idx_vals


def mean_dists(dists):
    return np.mean(dists, axis=0)


