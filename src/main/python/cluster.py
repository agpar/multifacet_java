#!/usr/bin/python3

"""
Clustering ideas:
    * By PCC as distance
        -> Problem: what to do with null PCCs
        -> Advantage: already calculated.
    * By vector dist between single user factors:
        -> Problem: Not yet computed, however, easy to compute.
        -> Problem: Does not include comparison of review evidence.
        -> Advantage: Defined for every user.
    * By item/category jaccard.
        -> Advantage: Already computed.
        -> Advantage: Defined for every user.
"""

import csv
import sys
from combine_vectors import combine_balanced_ids, combined_headers
from prediction_tools import INDEXES, init_indexes
from predict_pcc import learn_classifier
from data_set import DataSet
import os
from numbers import Number

from sklearn.cluster import AgglomerativeClustering
#from scipy.cluster.hierarchy import

import numpy as np

from regression import learn_logit

NUM_CLUSTERS = 10

aggClusterBuilder = AgglomerativeClustering(n_clusters=NUM_CLUSTERS, affinity='precomputed', linkage='average')


class IDIndexMap:
    def __init__(self):
        self.map = {}
        self._next_index = 0

    def get_int(self, item: str):
        if not isinstance(item, str):
            raise Exception("item must be a string.")
        if item in self.map:
            return self.map[item]
        else:
            index = self._next_index
            self._next_index += 1
            self.map[item] = index
            self.map[index] = item
            return index

    def get_str(self, item : int):
        if not isinstance(item, int):
            raise Exception("item must be an int.")
        if item in self.map:
            return self.map[item]
        else:
            raise Exception(f"No string is assigned to {item}")


def pcc_cluster(pairwise_path):

    def pcc_to_dist(pcc):
        return 1.0 - pcc

    def selector(line):
        pcc = line[INDEXES['PCC']]
        if pcc == 'null':
            # TODO: set this as average or negative?
            return pcc_to_dist(0.0)
        else:
            return pcc_to_dist(float(pcc))

    arr, index_map = pairwise_dist_matrix(pairwise_path, selector, 1.0)
    return ClusterClassifier(arr, index_map)


def social_jacc_cluster(pairwise_path):

    def social_jacc_to_dist(jacc):
        return 1.0 - jacc

    def selector(line):
        jacc = line[INDEXES['socialJacc']]
        return social_jacc_to_dist(float(jacc))

    arr, index_map = pairwise_dist_matrix(pairwise_path, selector, 1.0)
    return ClusterClassifier(arr, index_map)


def pairwise_dist_matrix(pairwise_path, selector, default_val):
    lines = []
    index_map = IDIndexMap()
    with open(pairwise_path, 'r') as f:
        header = [x.strip() for x in f.readline().split(',')]
        reader = csv.reader(f)
        for line in reader:
            user1 = index_map.get_int(line[0])
            user2 = index_map.get_int(line[1])
            val = selector(line)
            lines.append((user1, user2, val))

    num_keys = index_map._next_index
    arr = np.full((num_keys, num_keys), default_val, dtype=np.dtype("float16"))
    for idx1, idx2, val in lines:
        arr[idx1][idx2] = val
        arr[idx2][idx1] = val
    return arr, index_map


class ClusterClassifier:
    SINGLE_PATH = ""
    PAIRWISE_PATH = ""

    def __init__(self, dist_array: np.array, index_map: IDIndexMap):
        self.dist_array = dist_array
        self.index_map = index_map
        self.clusters = []
        self.classifiers = []

    def init_clusters(self):
        labels = aggClusterBuilder.fit_predict(self.dist_array)
        self.clusters = [set() for x in range(NUM_CLUSTERS)]
        for i in range(len(labels)):
            cluster_idx = labels[i]
            self.clusters[cluster_idx].add(self.index_map.get_str(i))

    def train_classifiers(self):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        for i in range(NUM_CLUSTERS):
            if len(self.clusters[i]) > 100:
                training_set = combine_balanced_ids(self.SINGLE_PATH, self.PAIRWISE_PATH, self.clusters[i])
                clf = learn_classifier(training_set, header, 1.0)
                self.classifiers.append(clf)


if __name__ == '__main__':
    if len(sys.argv) < 4:
        print("At least 3 arguments required: cluster.py {pcc|single} {path_to_single} {path_to_pairwise}")
        exit(1)

    cluster_type = sys.argv[1]
    single_path = sys.argv[2]
    pairwise_path = sys.argv[3]
    init_indexes(single_path, pairwise_path)
    if cluster_type not in {"pcc", "single"}:
        print(f"Cluster type must be 'pcc' or 'single', not {cluster_type}")
        exit(1)

    ClusterClassifier.SINGLE_PATH = single_path
    ClusterClassifier.PAIRWISE_PATH = pairwise_path
    clusters = pcc_cluster(pairwise_path)
    clusters.init_clusters()
    clusters.train_classifiers()
    print("Done")
