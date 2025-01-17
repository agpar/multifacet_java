#!/usr/bin/python3

"""Generates pairwise and single, clusters and predictions for an experiment."""
import argparse
import json
import os
import shutil
from multiprocessing import Process

import numpy as np
from scipy import sparse

import filter_yelp
import filter_epinions
import cluster
import predict
from vector_combiners.text_to_binary_converter import TextToBinaryConverter

if "MULTIFACET_ROOT" not in os.environ:
    raise Exception("$MULTIFACET_ROOT is not set. Can't determine where project is located.")
MULTIFACET_ROOT = os.environ["MULTIFACET_ROOT"]

SETUP_STEPS = ("filter", "single", "pairwise", "dists", "choose_k", "cluster", "predict", "tuples")
SETUP_STEP_FILES = {
    'filter': [],
    'single': ['single_feats.csv'],
    'pairwise': ['pairwise_feats.csv'],
    'dists':  ['pcc_sims.npz', 'social_sims.npz'],
    'choose_k': ['k_pcc_results.json', 'k_social_results.json'],
    'cluster': ['pcc_clusters.json', 'social_clusters.json'],
    'predict': [
        "global_pcc_predictions.txt",
        "global_social_predictions.txt",
        "global_real_friends.txt",
        "pcc_clustered_pcc_predictions.txt",
        "pcc_clustered_social_predictions.txt",
        "social_clustered_pcc_predictions.txt",
        "social_clustered_social_predictions.txt"
    ],
    'tuples': ['rating_tuples.txt']
}


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


def generate_single(data_set: DataSetEnum, paths):
    if data_set.IS_EPINIONS:
        executable = os.path.join(MULTIFACET_ROOT, "run.sh")
        os.system(f"{executable} --genSingle --epinions {paths['single_path']}")
    else:
        executable = os.path.join(MULTIFACET_ROOT, "src/main/python/generate_single_vects.py")
        os.system(f"python3 {executable} 0 1000000 {paths['single_path']}")
    TextToBinaryConverter(paths['single_path']).convert()


def generate_pairwise(data_set: DataSetEnum, paths):
    executable = os.path.join(MULTIFACET_ROOT, "run.sh")
    if data_set.IS_EPINIONS:
        os.system(f"{executable} --genPairs --epinions {paths['pairwise_path']}")
    else:
        os.system(f"{executable} --genPairs {paths['pairwise_path']}")
    TextToBinaryConverter(paths['pairwise_path']).convert()


def generate_dists(paths):
    pcc_dist_matrix = cluster.gen_sim_matrix(paths['single_path'], paths['pairwise_path'], "pcc")
    sparse.save_npz(paths['pcc_sim_matrix_path'], pcc_dist_matrix)

    social_dist_matrix = cluster.gen_sim_matrix(paths['single_path'], paths['pairwise_path'], "social")
    sparse.save_npz(paths['social_sim_matrix_path'], social_dist_matrix)


def generate_clusters(paths):
    cluster.run(paths['single_path'], paths['pairwise_path'], "pcc", paths['pcc_cluster_path'], [paths['pcc_sim_matrix_path']], None, 30, 60)
    cluster.run(paths['single_path'], paths['pairwise_path'], "social", paths['social_cluster_path'], [paths['social_sim_matrix_path']], None, 30, 60)


def generate_predictions(experiment_dir, paths):
    base_args = (paths['single_path'], paths['pairwise_path'])
    prediction_tasks = [
        (os.path.join(experiment_dir, "global_pcc_predictions.txt"), None, "pcc"),
        (os.path.join(experiment_dir, "global_social_predictions.txt"), None, "friend"),
        (os.path.join(experiment_dir, "global_real_friends.txt"), None, "realfriend"),
        (os.path.join(experiment_dir, "pcc_clustered_pcc_predictions.txt"), [paths['pcc_cluster_path']], "pcc"),
        (os.path.join(experiment_dir, "pcc_clustered_social_predictions.txt"), [paths['pcc_cluster_path']], "friend"),
        (os.path.join(experiment_dir, "social_clustered_pcc_predictions.txt"), [paths['social_cluster_path']], "pcc"),
        (os.path.join(experiment_dir, "social_clustered_social_predictions.txt"), [paths['social_cluster_path']], "friend")
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


def generate_rating_tuples(data_set, output_path):
    executable = os.path.join(MULTIFACET_ROOT, "run.sh")
    data_flag = "--epinions" if data_set.IS_EPINIONS else ""
    os.system(f"{executable} --genTuples {data_flag} {output_path}")


def delete_files_to_recreate(experiment_dir, skip_to_index):
    for i in range(len(SETUP_STEPS)):
        if i < skip_to_index:
            continue
        for fname in SETUP_STEP_FILES[SETUP_STEPS[i]]:
            full_path = os.path.join(experiment_dir, fname)
            if os.path.exists(full_path):
                new_path = full_path + ".old"
                shutil.move(full_path, new_path)


def should_run_step(step_name, skip_to):
    return SETUP_STEPS.index(step_name) >= skip_to


def generate_paths(experiment_dir):
    return {
        "single_path": os.path.join(experiment_dir, "single_feats.csv"),
        "single_bin": os.path.join(experiment_dir, "single_feats.csv").replace('.csv', '.npz'),
        "pairwise_path": os.path.join(experiment_dir, "pairwise_feats.csv"),
        "pairwise_bin": os.path.join(experiment_dir, "pairwise_feats.csv").replace('.csv', '.npz'),
        "pcc_sim_matrix_path": os.path.join(experiment_dir, "pcc_sims.npz"),
        "social_sim_matrix_path": os.path.join(experiment_dir, "social_sims.npz"),
        "pcc_cluster_path": os.path.join(experiment_dir, "pcc_clusters.json"),
        "social_cluster_path": os.path.join(experiment_dir, "social_clusters.json")
    }


def parse_skipto(skipto):
    if skipto is None:
        return SETUP_STEPS.index("filter")
    else:
        return  SETUP_STEPS.index(skipto)


def run(experiment_dir=None, data_set=None, skipto=None):
    step_to_skip_to = parse_skipto(skipto)

    data_set = DataSetEnum(data_set)
    if data_set.IS_EPINIONS:
        print(f"Setting up all data files for EPINIONS in {experiment_dir}")
    else:
        print(f"Setting up all data files for YELP in {experiment_dir}")

    if not os.path.exists(experiment_dir):
        os.makedirs(experiment_dir, exist_ok=True)

    paths = generate_paths(experiment_dir)
    delete_files_to_recreate(experiment_dir, step_to_skip_to)

    if should_run_step("filter", step_to_skip_to):
        print("Filtering data...")
        filter_data(data_set)

    if should_run_step("single", step_to_skip_to):
        print("Generating single user data...")
        generate_single(data_set, paths)

    if should_run_step("pairwise", step_to_skip_to):
        print("Generating pairwise...")
        generate_pairwise(data_set, paths['pairwise_path'])

    if should_run_step("dists", step_to_skip_to):
        print("Saving dist/sim matrices...")
        generate_dists(paths)

    if should_run_step("cluster", step_to_skip_to):
        print("Generating clusters...")

    if should_run_step("predict", step_to_skip_to):
        print("Running all predictions in parallel.")
        generate_predictions(experiment_dir, paths)

    if should_run_step("tuple", step_to_skip_to):
        rating_tuple_path = experiment_dir
        generate_rating_tuples(data_set, rating_tuple_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Perform all setup necessary to run a new set of prediction experiments.')
    parser.add_argument('experiment_dir',  type=str, help='The directory to store all the goodies.')
    parser.add_argument('data_set', type=str, choices={'yelp', 'epinions'}, help='Which data set to use.')
    parser.add_argument('--skipto', type=str, choices=SETUP_STEPS, default=SETUP_STEPS[0],
                        help='Skip to a certain step, assuming output of previous steps is already available.')
    args = parser.parse_args()
    run(**vars(args))
