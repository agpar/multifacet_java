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
from scipy import sparse

import clustering.clusteroid_kmeans as kmeans
from vector_combiners.text_vector_combiner import TextVectorCombiner


#TODO: Remember this can return NEGATIVE similaries and is not suitable for use with MCL
def pcc_sims(single_path, pairwise_path):
    vc = TextVectorCombiner(single_path, pairwise_path)
    pcc_index = vc.pairwise_header.index('PCC')

    def selector(line):
        pcc = line[pcc_index]
        if pcc == 'null':
            return 0
        else:
            return float(pcc)

    return pairwise_sim_matrix(single_path, pairwise_path, selector)


def social_jacc_sims(single_path, pairwise_path):

    vc = TextVectorCombiner(single_path, pairwise_path)
    social_index = vc.pairwise_header.index('socialJacc')

    def selector(line):
        return float(line[social_index])

    return pairwise_sim_matrix(single_path, pairwise_path, selector)


def pairwise_sim_matrix(single_path, pairwise_path, selector):
    count = 0
    # iterate through once to count the number of users
    for line in TextVectorCombiner.stream_csv(single_path):
        if line:
            count += 1

    arr = np.full((count, count), 0, dtype=np.dtype("float32"))
    with open(pairwise_path, 'r') as f:
        _ = f.readline()
        reader = csv.reader(f)
        for line in reader:
            user1_idx = int(line[0])
            user2_idx = int(line[1])
            val = selector(line)
            arr[user1_idx][user2_idx] = val
            arr[user2_idx][user1_idx] = val

    # Diagonal sim values must be 1.
    np.fill_diagonal(arr, 1)
    return sparse.csr_matrix(arr)


def gen_sim_matrix(single_path, pairwise_path, cluster_type):
    if cluster_type == "pcc":
        return pcc_sims(single_path, pairwise_path)
    elif cluster_type == "social":
        return social_jacc_sims(single_path, pairwise_path)
    else:
        print(f"Cluster type must be 'pcc' or 'social', not {cluster_type}")
        exit(1)


def sims_to_dists(arr, default_val=1):
    arr_copy = arr.copy()
    arr_copy.data *= -1
    arr_copy.data += 1

    dist_full = np.full(arr_copy.shape, default_val, dtype=np.float32)
    for i, r in enumerate(arr_copy):
        dist_full[i][r.indices] = r.data

    return dist_full


def run(single_path, pairwise_path, cluster_type, output_path, sims_in, sims_out, k, iters):
    sim_arr = None
    if sims_in:
        sim_arr = sparse.load_npz(sims_in[0])
    else:
        sim_arr = gen_sim_matrix(single_path, pairwise_path, cluster_type)
    if sims_out:
        sparse.save_npz(sims_out[0], sim_arr)

    if k == 1:
        clusters = np.array([0 for i in range(len(sim_arr))])
    else:
        dist_arr = sims_to_dists(sim_arr)
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
    parser.add_argument('--sims', dest='sims_in', type=str, nargs=1,
                        help='Path to precomputed distance matrix. Computed in memory if not provided.')
    parser.add_argument('--dumpsims', dest='sims_out', type=str, nargs=1,
                        help='Path to save computed dist matrix to, for future use with --dists')
    parser.add_argument('--k', type=int, help="Number of clusters to build.", default=30)
    parser.add_argument('--iters', type=int, help="Number of iterations of clutsering to perform", default=20)

    args = parser.parse_args()
    run(**vars(args))
