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

import numpy as np
from sklearn.cluster import AgglomerativeClustering

from combine_vectors import combine_balanced_friends, combined_headers, combine_balanced_pcc
from prediction_tools import INDEXES, init_indexes
import predict_pcc
import predict_friendship
from iter_cluster import iter_cluster_avg


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


def pcc_cluster(pairwise_path, single_path):

    def pcc_to_dist(pcc):
        return 1.0 - pcc

    def selector(line):
        pcc = line[INDEXES['PCC']]
        if pcc == 'null':
            # TODO: set this as average or negative?
            return pcc_to_dist(-1.0)
        else:
            return pcc_to_dist(float(pcc))

    arr, index_map = pairwise_dist_matrix(single_path, pairwise_path, selector, 0.0)
    return ClusterClassifier(arr, index_map)


def social_jacc_cluster(pairwise_path, single_path):

    def social_jacc_to_dist(jacc):
        return 1.0 - jacc

    def selector(line):
        jacc = line[INDEXES['socialJacc']]
        return social_jacc_to_dist(float(jacc))

    arr, index_map = pairwise_dist_matrix(single_path, pairwise_path, selector, 0.0)
    return ClusterClassifier(arr, index_map)


def pairwise_dist_matrix(single_path, pairwise_path, selector, default_val):
    index_map = IDIndexMap()
    # iterate through once to assign indexes to all users
    with open(single_path, 'r') as f:
        header = [x.strip() for x in f.readline().split(',')]
        reader = csv.reader(f)
        for line in reader:
            index_map.get_int(line[0])

    num_keys = index_map._next_index
    arr = np.full((num_keys, num_keys), default_val, dtype=np.dtype("float32"))
    with open(pairwise_path, 'r') as f:
        header = [x.strip() for x in f.readline().split(',')]
        reader = csv.reader(f)
        for line in reader:
            user1_idx = index_map.get_int(line[0])
            user2_idx = index_map.get_int(line[1])
            val = selector(line)
            arr[user1_idx][user2_idx] = val
            arr[user2_idx][user1_idx] = val
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
        labels = iter_cluster_avg(self.dist_array, int(len(self.dist_array)/ NUM_CLUSTERS))
        self.clusters = [set() for x in range(NUM_CLUSTERS)]
        for i in range(len(labels)):
            cluster_idx = labels[i]
            self.clusters[cluster_idx].add(self.index_map.get_str(i))
            self.user_clusters[self.index_map.get_str(i)] = cluster_idx
        cluster_lens = [len(c) for c in self.clusters]
        print(f"Cluster lengths: {cluster_lens}")

    def train_classifiers(self, combiner, clf_trainer):
        header = combined_headers(self.SINGLE_PATH, self.PAIRWISE_PATH)
        self.classifiers = [None for x in range(NUM_CLUSTERS)]
        for i in range(NUM_CLUSTERS):
            if len(self.clusters[i]) > 100:
                training_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, userIds=self.clusters[i])
                clf, score = clf_trainer(training_set, header, 1.0)
                print(score)
                print(clf.coef_)
                if score > 0.6:
                    self.classifiers[i] = clf
                else:
                    print("Score to low. Using generic classifier.")
        overall_set = combiner(self.SINGLE_PATH, self.PAIRWISE_PATH, numVects=300_000)
        self.overall_classifier, score = clf_trainer(overall_set, header, 1.0)
        print("Generic classifier.")
        print(score)
        print(self.overall_classifier.coef_)

    def fit(self, combiner, clf_trainer):
        self.init_clusters()
        self.train_classifiers(combiner, clf_trainer)

    def predict(self, user_id, lines):
        clf_idx = self.user_clusters[user_id]
        clf = self.classifiers[clf_idx]
        if clf is None:
            clf = self.overall_classifier

        return clf.predict(lines)


if __name__ == '__main__':
    if len(sys.argv) < 6:
        print("At least 5 arguments required: cluster.py {pcc|single} {pcc|friend} {path_to_single} {path_to_pairwise} {output_path}")
        exit(1)

    cluster_type = sys.argv[1]
    predict_type = sys.argv[2]
    single_path = sys.argv[3]
    pairwise_path = sys.argv[4]
    output_path = sys.argv[5]
    init_indexes(single_path, pairwise_path)
    ClusterClassifier.SINGLE_PATH = single_path
    ClusterClassifier.PAIRWISE_PATH = pairwise_path

    clusters = None
    if cluster_type == "pcc":
        print("Clustering by PCC")
        clusters = pcc_cluster(pairwise_path, single_path)
    elif cluster_type == "social":
        print("Clustering by socialJacc")
        clusters = social_jacc_cluster(pairwise_path, single_path)
    else:
        print(f"Cluster type must be 'pcc' or 'social', not {cluster_type}")
        exit(1)

    if predict_type == "pcc":
        print("Predicting PCC > 0")
        clusters.fit(combine_balanced_pcc, predict_pcc.learn_classifier)
        predict_pcc.output_predictions(single_path, pairwise_path, output_path, clusters)
    elif predict_type == "friend":
        print("Predicting friendship")
        clusters.fit(combine_balanced_friends, predict_friendship.learn_classifier)
        predict_friendship.output_predictions(single_path, pairwise_path, output_path, clusters)




