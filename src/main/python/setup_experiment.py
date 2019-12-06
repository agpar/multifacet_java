"""Generates pairwise and single, clusters and predictions for an experiment."""
import argparse
import json
import os
import shutil
from multiprocessing import Process

import numpy as np

import filter_yelp
import filter_epinions
import cluster
import predict
from clustering import clusteroid_kmeans
from clustering.choose_k import choose_k, eval_silouette

if "MULTIFACET_ROOT" not in os.environ:
    raise Exception("$MULTIFACET_ROOT is not set. Can't determine where project is located.")
MULTIFACET_ROOT = os.environ["MULTIFACET_ROOT"]


class DataSetEnum:
    def __init__(self, data_str):
        if data_str not in ('yelp', 'epinions'):
            raise ValueError("Bad data set arg.")
        self.data_str = data_str

    @property
    def IS_EPINIONS(self):
        if self.data_str == 'epinions':
            return True
        return False

    @property
    def IS_YELP(self):
        if self.data_str == 'yelp':
            return True
        return False


def filter_data(data_set: DataSetEnum):
    if data_set.IS_YELP:
        filter_yelp.run()
    elif data_set.IS_EPINIONS:
        filter_epinions.run()


def generate_single(data_set: DataSetEnum, output_path):
    if data_set.IS_EPINIONS:
        executable = os.path.join(MULTIFACET_ROOT, "run.sh")
        os.system(f"{executable} --genSingle --epinions {output_path}")
    else:
        executable = os.path.join(MULTIFACET_ROOT, "src/main/python/generate_single_vects.py")
        os.system(f"python3 {executable} 0 1000000 {output_path}")


def generate_pairwise(data_set: DataSetEnum, output_path):
    executable = os.path.join(MULTIFACET_ROOT, "run.sh")
    if data_set.IS_EPINIONS:
        os.system(f"{executable} --genPairs --epinions {output_path}")
    else:
        os.system(f"{executable} --genPairs {output_path}")


def generate_predictions(experiment_dir, single_path, pairwise_path, pcc_cluster_path, social_cluster_path):
    base_args = (single_path, pairwise_path)
    prediction_tasks = [
        (os.path.join(experiment_dir, "global_pcc_predictions.txt"), None, "pcc"),
        (os.path.join(experiment_dir, "global_social_predictions.txt"), None, "friend"),
        (os.path.join(experiment_dir, "global_real_friends.txt"), None, "realfriend"),
        (os.path.join(experiment_dir, "pcc_clustered_pcc_predictions.txt"), pcc_cluster_path, "pcc"),
        (os.path.join(experiment_dir, "pcc_clustered_social_predictions.txt"), pcc_cluster_path, "friend"),
        (os.path.join(experiment_dir, "social_clustered_pcc_predictions.txt"), social_cluster_path, "pcc"),
        (os.path.join(experiment_dir, "social_clustered_social_predictions.txt"), social_cluster_path, "friend")
    ]
    processes = []
    for task in prediction_tasks:
        if os.path.exists(task[0]):
            shutil.move(task[0], task[0] + ".old")
        p = Process(target=predict.run, args=(base_args + task))
        p.start()
        processes.append(p)
    for p in processes:
        p.join()


def run(experiment_dir=None, data_set=None):
    data_set = DataSetEnum(data_set)
    if data_set.IS_EPINIONS:
        print(f"Setting up all data files for EPINIONS in {experiment_dir}")
    else:
        print(f"Setting up all data files for YELP in {experiment_dir}")

    if not os.path.exists(experiment_dir):
        os.makedirs(experiment_dir, exist_ok=True)

    print("Filtering data...")
    filter_data(data_set)

    print("Generating single user data...")
    single_path = os.path.join(experiment_dir, "single_feats.csv")
    generate_single(data_set, single_path)

    print("Generating pairwise...")
    pairwise_path = os.path.join(experiment_dir, "pairwise_feats.csv")
    generate_pairwise(data_set, pairwise_path)

    print("Saving dist matrices...")
    pcc_dist_matrix_path = os.path.join(experiment_dir, "pcc_dists.npy")
    pcc_dist_matrix = cluster.gen_dist_matrix(single_path, pairwise_path, "pcc")
    np.save(pcc_dist_matrix_path, pcc_dist_matrix)

    social_dist_matrix_path = os.path.join(experiment_dir, "social_dists.npy")
    social_dist_matrix = cluster.gen_dist_matrix(single_path, pairwise_path, "social")
    np.save(social_dist_matrix_path, social_dist_matrix)

    print("Determining optimal cluster count...")
    k_goodness_pcc = choose_k(clusteroid_kmeans.cluster, pcc_dist_matrix_path, range(20, 80), 30, eval_silouette)
    with open(os.path.join(experiment_dir, "k_pcc_results.json"), 'w') as f:
        json.dump(k_goodness_pcc, f)

    k_goodness_social = choose_k(clusteroid_kmeans.cluster, social_dist_matrix_path, range(20, 80), 30, eval_silouette)
    with open(os.path.join(experiment_dir, "k_social_results.json"), 'w') as f:
        json.dump(k_goodness_social, f)

    print("Generating clusters...")
    pcc_cluster_path = os.path.join(experiment_dir, "pcc_clusters.json")
    cluster.run(single_path, pairwise_path, "pcc", pcc_cluster_path, [pcc_dist_matrix_path], None, k_goodness_pcc[0][0], 50)
    #cluster.run(single_path, pairwise_path, "pcc", pcc_cluster_path, [pcc_dist_matrix_path], None, 50, 50)

    social_cluster_path = os.path.join(experiment_dir, "social_clusters.json")
    cluster.run(single_path, pairwise_path, "social", social_cluster_path, [social_dist_matrix_path], None, k_goodness_social[0][0], 50)
    #cluster.run(single_path, pairwise_path, "social", social_cluster_path, [social_dist_matrix_path], None, 50, 50)

    print("Running all predictions in parallel.")
    generate_predictions(experiment_dir, single_path, pairwise_path, pcc_cluster_path, social_cluster_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Perform all setup necessary to run a new set of prediction experiments.')
    parser.add_argument('experiment_dir',  type=str, help='The directory to store all the goodies.')
    parser.add_argument('data_set', type=str, choices={'yelp', 'epinions'}, help='Which data set to use.')
    args = parser.parse_args()
    run(**vars(args))