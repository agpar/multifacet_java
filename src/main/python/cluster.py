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
import os
from numbers import Number

from sklearn.cluster import AgglomerativeClustering
#from scipy.cluster.hierarchy import

import numpy as np

from regression import learn_logit

NUM_CLUSTERS = 10
INDEXES = {}

clusterBuilder = AgglomerativeClustering(n_clusters=NUM_CLUSTERS, affinity='precomputed', linkage='average')


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


def pcc_dist_matrix(pairwise_path):

    def pcc_to_dist(pcc):
        return (pcc + 1.0) / 2

    def selector(line):
        pcc = line[INDEXES['PCC']]
        if pcc == 'null':
            # TODO: set this as average or negative?
            return pcc_to_dist(0.0)
        else:
            return pcc_to_dist(float(pcc))

    return pairwise_dist_matrix(pairwise_path, selector)


def social_jacc_dist_matrix(pairwise_path):

    def social_jacc_to_dist(jacc):
        return 1 / (1 + jacc)

    def selector(line):
        jacc = line[INDEXES['social_jacc']]
        return social_jacc_to_dist(float(jacc))

    return pairwise_dist_matrix(pairwise_path, selector)


def pairwise_dist_matrix(pairwise_path, selector):
    lines = []
    index_map = IDIndexMap()
    with open(pairwise_path, 'r') as f:
        header = f.readline()
        INDEXES['PCC'] = header.index("PCC")
        INDEXES['social_jacc'] = header.index("socialJacc")
        reader = csv.reader(f)
        for line in reader:
            user1 = index_map.get_int(line[0])
            user2 = index_map.get_int(line[1])
            val = selector(line)
            lines.append((user1, user2, val))

    num_keys = len(index_map.map)
    arr = np.zeros((num_keys, num_keys), dtype=np.dtype("float16"))
    for idx1, idx2, val in lines:
        arr[idx1][idx2] = val
        arr[idx2][idx1] = val
    return arr


class ClusterClassifier:
    def __init__(self, dist_array: np.array, index_map: IDIndexMap):
        self.dist_array = dist_array
        self.index_map = index_map
        self.clusters = []
        self.classifiers = []

    def init_clusters(self):
        self.clusters = clusterBuilder.fit_predict(self.dist_array)

    def train_classifiers(self):
        for i in range(NUM_CLUSTERS):
            user_ids = [self.index_map.get_str(idx) for idx, x in enumerate(self.clusters) if x == i]
            # TODO: stream the vectors from a file
            # TODO: Learn the classifier.


if __name__ == '__main__':
    if len(sys.argv < 3):
        print("At least 3 arguments required: cluster.py {pcc|single} {path_to_file}")
        exit(1)

    cluster_type = sys.argv[1]
    data_path = sys.argv[-1]
    if cluster_type not in {"pcc", "single"}:
        print(f"Cluster type must be 'pcc' or 'single', not {cluster_type}")
        exit(1)

    if not os.path.exists(data_path):
        print(f"Data file does not exist at {data_path}")

    clusters = clusterBuilder.fit(pcc_dist_matrix(data_path))
