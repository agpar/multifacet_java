import numpy as np
from sklearn.metrics import silhouette_score
from collections import defaultdict


def eval_both(dists, cluster_labels):
    return (eval_silhouette(dists, cluster_labels),
            average_intra_clust_distance(dists, labels_to_clusters(cluster_labels)))


def eval_silhouette(dists, cluster_labels):
    return float(silhouette_score(dists, cluster_labels, metric='precomputed'))


def average_intra_clust_distance(dists, clusters):
    avg_dist = 0
    cluster_len = 0
    for cluster in clusters.values():
        if cluster:
            cluster_list = list(cluster)
            intra_dists = np.array([d[cluster_list] for d in dists[cluster_list]])
            avg = np.nanmean(intra_dists)
            avg_dist += avg
            cluster_len += 1
    return avg_dist / cluster_len


def median_intra_clust_distance(dists, clusters):
    avg_med = 0
    for cluster in clusters.values():
        cluster_list = sorted(list(cluster))
        intra_dists = np.array([d[cluster_list] for d in dists[cluster_list]])
        median = np.median(intra_dists)
        avg_med += median
    return avg_med / len(clusters)


def clusters_to_labels(dists, clusters):
    """Transform a dict of cluster assignments to an array of labels"""
    labels = np.full(len(dists), -1)
    for key, vals in clusters.items():
        for val in vals:
            labels[val] = key
    return labels


def labels_to_clusters(labels):
    clusters = defaultdict(set)
    for i in range(len(labels)):
        clusters[labels[i]].add(i)
    return clusters


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


class AvgClusterDistCalculator:
    def __init__(self, dist_matrix, clusters):
        """Init an AvgClusterDistCalculator

        :param dist_matrix: A N*N np.array dist matrix between points.
        :param clusters: A dict : int -> set(int) of clusters, like produced by labels_to_clusters()
        """
        self.dist_matrix = dist_matrix
        self.dist_masked = self._mask_diag()
        self.dist_max = np.nanmax(dist_matrix).item()
        self.clusters = clusters

    def avg_dist_psuedo_means(self):
        new_clusteroids = np.empty((len(self.clusters), self.dist_matrix.shape[0]))

        for key in sorted(list(self.clusters.keys())):
            cluster = self.clusters[key]
            if cluster:
                new_clusteroids[key] = np.ma.mean(self.dist_masked[list(cluster)], axis=0)
            else:
                new_clusteroids[key] = np.full(self.dist_matrix.shape[0], self.dist_max)
        return new_clusteroids

    def _mask_diag(self):
        diag_indices = np.diag_indices(self.dist_matrix.shape[0])
        mask_array = np.zeros(self.dist_matrix.shape, dtype=np.dtype('byte'))
        mask_array[diag_indices] = 1
        masked_view = self.dist_matrix.view(np.ma.MaskedArray)
        masked_view.mask = mask_array
        return masked_view

