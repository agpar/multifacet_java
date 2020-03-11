import argparse
import json
import pickle
import os

from prediction.actual_friendship import RealFriendTrainer
from prediction.cluster_classifier import ClusterClassifier
from prediction.predict_friendship import FriendshipTrainer
from prediction.predict_pcc import PCCTrainer
from vector_combiners import *
from vector_combiners.vector_combiner_builder import VectorCombinerBuilder


def write_predictions(stream, clf, path, predict_proba=True):
    with open(path, 'w') as f:
        for pair in stream:
            pair_ids, pair_vect = pair
            user1_id, user2_id = pair_ids
            if predict_proba:
                pred1 = clf.predict_proba(user1_id, [pair_vect])[0]
                if pred1 > 0.5:
                    f.write(f"{user1_id} {user2_id} {pred1}\n")
            else:
                pred1 = int(clf.predict(user1_id, [pair_vect])[0])
                if pred1:
                    f.write(f"{user1_id} {user2_id} {pred1}\n")


def run(single_path, pairwise_path, output_path, cluster_path, target):
    vector_combiner = VectorCombinerBuilder.build(single_path, pairwise_path)

    # Assign all users to a single cluster if no clusters are specified
    if cluster_path is not None:
        with open(cluster_path[0]) as f:
            clusters = json.load(f)
    else:
        clusters = [0 for i in range(vector_combiner.user_count())]

    if target == 'pcc':
        classifier = ClusterClassifier(clusters, BalancedPCCDataSetBuilder, PCCTrainer(vector_combiner.header))
    elif target == 'friend':
        classifier = ClusterClassifier(clusters, BalancedFriendDataSetBuilder, FriendshipTrainer(vector_combiner.header))
    elif target == 'realfriend':
        classifier = ClusterClassifier(clusters, BalancedDataSetBuilder, RealFriendTrainer(vector_combiner.header))
    else:
        print(f"Unknown target of prediction: {target}")
        exit(1)

    output_dir = "/".join(output_path.split("/")[:-1])
    model_name = output_path.split("/")[-1].split('.')[0] + "_model"

    if os.path.exists(os.path.join(output_dir, model_name)):
        print("Reusing dumped classifier.")
        with open(os.path.join(output_dir, model_name), 'rb') as f:
            classifier = pickle.load(f)
    else:
        classifier.SINGLE_PATH = single_path
        classifier.PAIRWISE_PATH = pairwise_path
        classifier.fit()

    with open(os.path.join(output_dir, model_name), 'wb') as f:
        pickle.dump(classifier, f)

    vector_stream = map(classifier.trainer.filter_target, VectorCombinerBuilder.build(single_path, pairwise_path).stream())
    write_predictions(vector_stream, classifier, output_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate trust link predictions.')
    parser.add_argument('single_path',  type=str,
                        help='Path to single-agent feature csv or npz.')
    parser.add_argument('pairwise_path', type=str,
                        help='Path to pairwise-agent feature csv or npz.')
    parser.add_argument('output_path',  type=str,
                        help="Path to a file where output clusters are written.")
    parser.add_argument('target', type=str, choices={'pcc', 'friend', 'realfriend'},
                        help="The target of prediction.")
    parser.add_argument('--clusters', dest='cluster_path', type=str, nargs=1, default=None,
                        help="Path to cluster assignments. If empty, assume one global cluster.")
    args = parser.parse_args()
    run(**vars(args))
