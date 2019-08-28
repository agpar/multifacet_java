import numpy as np


def iter_cluster(dists: np.array, clust_size):
    already_clustered = set()
    clusters = {}
    cluster_num = 0
    means = sorted_with_idx(mean_dists(dists))
    while len(dists) - len(already_clustered) > clust_size:
        next_center_idx = next_idx_to_cluster(means, already_clustered)
        cluster = closest_points(dists[next_center_idx], clust_size, already_clustered)
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


def closest_points(dists, n, already_clustered):
    cluster = set()
    for idx, dist in sorted_with_idx(dists):
        if idx not in already_clustered:
            cluster.add(idx)
        if len(cluster) >= n:
            break
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


