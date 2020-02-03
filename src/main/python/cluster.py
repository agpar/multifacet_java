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
import json
import argparse
from sys import exit

import numpy as np

from prediction_tools import INDEXES, init_indexes
import clustering.clusteroid_kmeans as kmeans


def pcc_dists(single_path, pairwise_path):

    def pcc_to_dist(pcc):
        return 1.0 - pcc

    def selector(line):
        pcc = line[INDEXES['PCC']]
        if pcc == 'null':
            return pcc_to_dist(0.0)
        else:
            return pcc_to_dist(float(pcc))

    return pairwise_dist_matrix(single_path, pairwise_path, selector, pcc_to_dist(0))


def social_jacc_dists(single_path, pairwise_path):

    def social_jacc_to_dist(jacc):
        return 1.0 - jacc

    def selector(line):
        jacc = line[INDEXES['socialJacc']]
        return social_jacc_to_dist(float(jacc))

    return pairwise_dist_matrix(single_path, pairwise_path, selector, social_jacc_to_dist(0))


def pairwise_dist_matrix(single_path, pairwise_path, selector, default_val):
    count = 0
    # iterate through once to count the number of users
    with open(single_path, 'r') as f:
        # throw away header
        _ = f.readline()
        for line in f:
            if line:
                count += 1

    arr = np.full((count, count), default_val, dtype=np.dtype("float32"))
    with open(pairwise_path, 'r') as f:
        _ = f.readline()
        reader = csv.reader(f)
        for line in reader:
            user1_idx = int(line[0])
            user2_idx = int(line[1])
            val = selector(line)
            arr[user1_idx][user2_idx] = val
            arr[user2_idx][user1_idx] = val

    # Diagonal dist values must be 0.
    np.fill_diagonal(arr, 0)
    return arr


def gen_dist_matrix(single_path, pairwise_path, cluster_type):
    init_indexes(single_path, pairwise_path)
    if cluster_type == "pcc":
        return pcc_dists(single_path, pairwise_path)
    elif cluster_type == "social":
        return social_jacc_dists(single_path, pairwise_path)
    else:
        print(f"Cluster type must be 'pcc' or 'social', not {cluster_type}")
        exit(1)


def run(single_path, pairwise_path, cluster_type, output_path, dists_in, dists_out, k, iters):
    init_indexes(single_path, pairwise_path)
    dist_arr = None
    if dists_in:
        dist_arr = np.load(dists_in[0])
    else:
        dist_arr = gen_dist_matrix(single_path, pairwise_path, cluster_type)
    if dists_out:
        np.save(dists_out[0], dist_arr)

    clusters = kmeans.cluster(dist_arr, k, iters)
    with open(output_path, 'w') as f:
        f.write(json.dumps(clusters.tolist()))
    return clusters


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate and output clusters of users.')
    parser.add_argument('single_path',  type=str,
                        help='Path to single-agent feature csv.')
    parser.add_argument('pairwise_path', type=str,
                        help='Path to pairwise-agent feature csv.')
    parser.add_argument('cluster_type', type=str,  choices={'pcc', 'social'},
                        help="The basis for the distance metric used to compute clusters.")
    parser.add_argument('output_path',  type=str,
                        help="Path to a file where output clusters are written.")
    parser.add_argument('--dists', dest='dists_in', type=str, nargs=1,
                        help='Path to precomputed distance matrix. Computed in memory if not provided.')
    parser.add_argument('--dumpdists', dest='dists_out', type=str, nargs=1,
                        help='Path to save computed dist matrix to, for future use with --dists')
    parser.add_argument('--k', type=int, help="Number of clusters to build.", default=30)
    parser.add_argument('--iters', type=int, help="Number of iterations of clutsering to perform", default=20)

    args = parser.parse_args()
    run(**vars(args))
