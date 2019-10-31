import argparse
import json

from prediction.actual_friendship import RealFriendTrainer
from prediction.cluster_classifier import ClusterClassifier
from prediction.predict_friendship import FriendshipTrainer
from combine_vectors import combine_balanced_friends, combine_balanced_pcc, combine_stream
from prediction.predict_pcc import PCCTrainer
from prediction_tools import init_indexes, stream_csv, write_predictions


def run(single_path, pairwise_path, output_path, cluster_path, target):
    init_indexes(single_path, pairwise_path)

    # Assign all users to a single cluster if no clusters are specified
    if cluster_path is not None:
        with open(cluster_path[0]) as f:
            clusters = json.load(f)
    else:
        clusters = []
        for line in stream_csv(single_path):
            clusters.append(0)

    if target == 'pcc':
        classifier = ClusterClassifier(clusters, combine_balanced_pcc, PCCTrainer())
    elif target == 'friend':
        classifier = ClusterClassifier(clusters, combine_balanced_friends, FriendshipTrainer())
    elif target == 'realfriend':
        classifier = ClusterClassifier(clusters, combine_stream, RealFriendTrainer())
    else:
        print(f"Unknown target of prediction: {target}")
        exit(1)

    classifier.SINGLE_PATH = single_path
    classifier.PAIRWISE_PATH = pairwise_path
    classifier.fit()

    write_predictions(combine_stream, classifier, output_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate trust link predictions.')
    parser.add_argument('single_path',  type=str,
                        help='Path to single-agent feature csv.')
    parser.add_argument('pairwise_path', type=str,
                        help='Path to pairwise-agent feature csv.')
    parser.add_argument('output_path',  type=str,
                        help="Path to a file where output clusters are written.")
    parser.add_argument('target', type=str, choices={'pcc', 'friend', 'realfriend'},
                        help="The target of prediction.")
    parser.add_argument('--clusters', dest='cluster_path', type=str, nargs=1, default=None,
                        help="Path to cluster assignments. If empty, assume one global cluster.")
    args = parser.parse_args()
    run(**vars(args))
