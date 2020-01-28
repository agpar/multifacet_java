import json
import os

from clustering import clusteroid_kmeans
from clustering import greedy_one_shot
from clustering.choose_k import choose_k
from clustering.cluster_tools import eval_both


def run_experiment(experiment_dir, pcc_dist_matrix_path, social_dist_matrix_path):
    k_range = list(range(2, 100))
    iters = 20

    k_pcc_greedy = choose_k(greedy_one_shot.cluster_k, pcc_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_pcc_greedy.json"), 'w') as f:
        json.dump(k_pcc_greedy, f)

    k_social_greedy = choose_k(greedy_one_shot.cluster_k, social_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_social_greedy.json"), 'w') as f:
        json.dump(k_social_greedy, f)

    k_pcc_iter = choose_k(clusteroid_kmeans.cluster, pcc_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_pcc_iter.json"), 'w') as f:
        json.dump(k_pcc_iter, f)

    k_social_iter = choose_k(clusteroid_kmeans.cluster, social_dist_matrix_path, k_range, iters, eval_both)
    with open(os.path.join(experiment_dir, "k_social_iter.json"), 'w') as f:
        json.dump(k_social_iter, f)

