#!/usr/bin/python3

"""
Clustering ideas:
    * By PCC as distance
        -> Problem: what to do with null PCCs
        -> Problem: Non euclidean.
        -> Advantage: already calculated.
    * By vector dist between single user factors:
        -> Problem: Not yet computed, however, easy to compute.
        -> Problem: Does not include comparison of review evidence.
        -> Advantage: Defined for every user.
        -> Advantage: Euclidean.
    * By review score vectors in dimension reduced space.
        -> Advantage: Captures groups of users with similar interests
        -> Advantage: Can use euclidean distance measure.
        -> A pain to compute.
    * By item/category jaccard.
        -> Advantage: Already computed.
        -> Advantage: Defined for every user.
        -> Problem: Non euclidean.


    Clustering in non-euclidean formulation:
        * Chose as next "center" the point in the current cluster with the lowest squared
          distance to all other points in the cluster.
"""

import csv
import sys
from sys import exit

import numpy as np
from sklearn.cluster import AgglomerativeClustering

from combine_vectors import combine_balanced_friends, combine_balanced_pcc
from prediction_tools import INDEXES, init_indexes
import predict_pcc
import predict_friendship
from settings import NUM_CLUSTERS
from clustering.cluster_classifier import ClusterClassifier
from tools.id_index_map import IDIndexMap


aggClusterBuilder = AgglomerativeClustering(n_clusters=NUM_CLUSTERS, affinity='precomputed', linkage='average')


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




