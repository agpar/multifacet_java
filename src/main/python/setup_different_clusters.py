from setup_experiment import *
from multiprocessing import Pool


def cluster_and_predict(step_to_skip_to, experiment_dir, single_path, pairwise_path, single_bin, pairwise_bin, sim_matrix_path, k, cluster_type, predict_type):
    cluster_path = os.path.join(experiment_dir, f"{cluster_type}_{k}_clusters.json")
    if should_run_step("cluster", step_to_skip_to):
        cluster.run(single_path, pairwise_path, cluster_type, cluster_path, [sim_matrix_path], None, k, 60)

    if should_run_step("predict", step_to_skip_to):
        predict.run(single_bin,
                    pairwise_bin,
                    os.path.join(experiment_dir, f"{cluster_type}_clustered_{k}_{predict_type}_predictions.txt"),
                    [cluster_path],
                    predict_type)


def run(experiment_dir=None, data_set=None, skipto=None, krange=None):
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

    pool = Pool(8)
    for k in krange:
        args = (
            step_to_skip_to,
            experiment_dir,
            paths['single_path'],
            paths['pairwise_path'],
            paths['single_bin'],
            paths['pairwise_bin'],
            paths['social_sim_matrix_path'],
            k,
            'pcc',
            'social')
        pool.apply_async(cluster_and_predict, args=args)
    pool.close()
    pool.join()

    rating_tuple_path = experiment_dir
    generate_rating_tuples(data_set, rating_tuple_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Perform all setup necessary to run a new set of prediction experiments.')
    parser.add_argument('experiment_dir',  type=str, help='The directory to store all the goodies.')
    parser.add_argument('data_set', type=str, choices={'yelp', 'epinions'}, help='Which data set to use.')
    parser.add_argument('--skipto', type=str, choices=SETUP_STEPS, default=SETUP_STEPS[0],
                        help='Skip to a certain step, assuming output of previous steps is already available.')
    parser.add_argument('--krange', type=str, help='Which range of k to use, e.g. 0-20', default="0-20")

    args = parser.parse_args()
    args.krange = list(range(*map(int, args.krange.split("-"))))
    run(**vars(args))
