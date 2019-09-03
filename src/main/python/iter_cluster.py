import numpy as np
import random

ITERS = 0


def iter_cluster_greedy(dists: np.array, clust_size):
    return modified_knn_cluster(dists, clust_size, closest_points_to_center)


def iter_cluster_avg(dists: np.array, clust_size):
    return modified_knn_cluster(dists, clust_size, closest_points_average)


def modified_knn_cluster(dists: np.array, clust_size, cluster_builder):
    # First cluster step
    means = sorted_with_idx(mean_dists(dists))
    clusters = refine_clusters(dists, means, clust_size, cluster_builder)

    for i in range(ITERS):
        clusters = refine_clusters(dists, means, clust_size, cluster_builder, clusters)

    # Output cluster
    cluster_labels = np.full(len(dists), -1)
    for key, vals in clusters.items():
        for val in vals:
            cluster_labels[val] = key
    return cluster_labels


def refine_clusters(dists, means, clust_size, cluster_builder, old_clusters=None):
    already_clustered = set()
    clusters = {}
    cluster_num = 0
    if old_clusters:
        for old_cluster in random.shuffle(old_clusters.values()):
            next_center_idx = most_central_idx(means, dists, already_clustered, old_cluster)
            cluster = cluster_builder(next_center_idx, dists, clust_size, already_clustered)
            already_clustered = already_clustered.union(cluster)
            clusters[cluster_num] = cluster
            cluster_num += 1
    else:
        while len(dists) - len(already_clustered) > clust_size:
            next_center_idx = most_central_idx(means, dists, already_clustered)
            cluster = cluster_builder(next_center_idx, dists, clust_size, already_clustered)
            already_clustered = already_clustered.union(cluster)
            clusters[cluster_num] = cluster
            cluster_num += 1

    last_cluster = set([idx for idx, _ in enumerate(dists) if idx not in already_clustered])
    clusters[cluster_num] = last_cluster
    return clusters


def most_central_idx(sorted_means, dists, already_clustered, relative_to=None):
    if relative_to is None:
        return next_idx_to_cluster(sorted_means, already_clustered)

    idx_list = sorted(list(c for c in relative_to if c not in already_clustered))
    cluster_dists = [d[idx_list] for d in dists[idx_list]]
    means = [np.mean(clu_dist) for clu_dist in cluster_dists]
    min_mean = min(means)
    min_mean_idx = means.index(min_mean)
    return idx_list[min_mean_idx]


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


