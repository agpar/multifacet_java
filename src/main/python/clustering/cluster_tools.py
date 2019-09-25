import math
import numpy as np
from sklearn.metrics import silhouette_score
from collections import defaultdict


def eval(dists, cluster_labels):
    return silhouette_score(dists, cluster_labels, metric='precomputed')


def average_intra_clust_distance(dists, clusters):
    avg_dist = 0
    for cluster in clusters.values():
        cluster_list = list(cluster)
        intra_dists = np.array([d[cluster_list] for d in dists[cluster_list]])
        avg = np.mean(intra_dists)
        avg_dist += avg
    return avg_dist / len(clusters)


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
        self.clusters = clusters
        self.cluster_dists = {}

        self.__init_clusters()

    def __init_clusters(self):
        for key, cluster in self.clusters.items():
            cluster_dists = self.dist_matrix[list(cluster)]
            self.cluster_dists[key] = self.__aggregate_dists(cluster_dists)

    def __aggregate_dists(self, cluster_dists):
        return np.mean(cluster_dists, axis=1)

    def avg_dist(self, idx, cluster_key):
        return self.cluster_dists[cluster_key][idx]

    def sim_clusteroid(self, cluster_key):
        return self.cluster_dists[cluster_key]

    def nearest_cluster(self, idx):
        min_val = math.inf
        min_ind = -1
        for key, avgs in self.cluster_dists:
            if avgs[idx] < min_val:
                min_val = avgs[idx]
                min_ind = key
        return min_ind
