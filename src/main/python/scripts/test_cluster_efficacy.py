import json
import os

import sys
sys.path.append("..")
from clustering import clusteroid_kmeans
from clustering import greedy_one_shot
from clustering.choose_k import choose_k
from clustering.cluster_tools import eval_both


def run_experiment(experiment_dir, pcc_dist_matrix_path, social_dist_matrix_path):
    k_range = list(range(2, 100))
    iters = 20

    print("Starting iterative social cluster tests")
    k_social_iter = choose_k(clusteroid_kmeans.cluster, social_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_social_iter.json"), 'w') as f:
        json.dump(k_social_iter, f)

    print("Starting iterative pcc cluster tests")
    k_pcc_iter = choose_k(clusteroid_kmeans.cluster, pcc_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_pcc_iter.json"), 'w') as f:
        json.dump(k_pcc_iter, f)


    print("Starting greedy pcc cluster tests")
    k_pcc_greedy = choose_k(greedy_one_shot.cluster_k, pcc_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_pcc_greedy.json"), 'w') as f:
        json.dump(k_pcc_greedy, f)

    print("Starting social pcc cluster tests")
    k_social_greedy = choose_k(greedy_one_shot.cluster_k, social_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_social_greedy.json"), 'w') as f:
        json.dump(k_social_greedy, f)

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: python3 test_cluster_efficacy.py {path_to_experiment_dir} {path_to_pcc_dists} {path_to_social_dists}")
        sys.exit(1)
    run_experiment(sys.argv[1], sys.argv[2], sys.argv[3])

