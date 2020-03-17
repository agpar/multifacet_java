# coding: utf-8

import os
import sys
import json
from machine_learning.surprise_experiments import *

def run_predictions(EXPERIMENT_DIR):
    files_in_dir = os.listdir(EXPERIMENT_DIR)
    prediction_files = [os.path.join(EXPERIMENT_DIR, f) for f in files_in_dir if f.endswith('predictions.txt')]

    experiments = []
    prediction_files.sort(key=lambda x : int(x.split('_')[-3]))
    for trust_file in prediction_files:
        experiments.append(Experiment(EXPERIMENT_DIR, trust_file, .3, 50))

    all_results = evaluate_all_links(experiments)
    print(all_results)
    with open('run_results.json', 'w') as f:
        json.dump(all_results, f)


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} experiment_dir")
        exit(1)
    run_predictions(sys.argv[1])


