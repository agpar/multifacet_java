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
from combine_vectors import combine_balanced_ids, combined_headers, combine_balanced_num, combine_stream
from prediction_tools import INDEXES, init_indexes
from predict_pcc import learn_classifier, output_predictions
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
    CLUSTERED = True

    def __init__(self, dist_array: np.array, index_map: IDIndexMap):
        self.dist_array = dist_array
        self.index_map = index_map
        self.user_clusters = {}
        self.clusters = []
        self.classifiers = []
        self.overall_classifier = None

    def init_clusters(self):
        labels = aggClusterBuilder.fit_predict(self.dist_array)
        self.clusters = [set() for x in range(NUM_CLUSTERS)]
        self.classifiers = [None for x in range(NUM_CLUSTERS)]
        for i in range(len(labels)):
            cluster_idx = labels[i]
            self.clusters[cluster_idx].add(self.index_map.get_str(i))
            self.user_clusters[self.index_map.get_str(i)] = cluster_idx

    def train_classifiers(self):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        for i in range(NUM_CLUSTERS):
            if len(self.clusters[i]) > 100:
                training_set = combine_balanced_ids(self.SINGLE_PATH, self.PAIRWISE_PATH, self.clusters[i])
                clf = learn_classifier(training_set, header, 1.0)
                print(clf.coef_)
                self.classifiers[i] = clf
        overall_set = combine_balanced_num(self.SINGLE_PATH, self.PAIRWISE_PATH, 200_000)
        self.overall_classifier = learn_classifier(overall_set, header, 1.0)

    def fit(self):
        self.init_clusters()
        self.train_classifiers()

    def predict(self, user_id, lines):
        clf_idx = self.user_clusters[user_id]
        clf = self.classifiers[clf_idx]
        if clf is None:
            clf = self.overall_classifier

        return clf.predict(lines)


if __name__ == '__main__':
    if len(sys.argv) < 5:
        print("At least 3 arguments required: cluster.py {pcc|single} {path_to_single} {path_to_pairwise} {output_path}")
        exit(1)

    cluster_type = sys.argv[1]
    single_path = sys.argv[2]
    pairwise_path = sys.argv[3]
    output_path = sys.argv[4]
    init_indexes(single_path, pairwise_path)
    if cluster_type not in {"pcc", "single"}:
        print(f"Cluster type must be 'pcc' or 'single', not {cluster_type}")
        exit(1)

    ClusterClassifier.SINGLE_PATH = single_path
    ClusterClassifier.PAIRWISE_PATH = pairwise_path
    clusters = pcc_cluster(pairwise_path)
    clusters.fit()

    stream = combine_stream(single_path, pairwise_path)
    output_predictions(single_path, pairwise_path, output_path, clusters)

