import argparse
import json

from prediction.cluster_classifier import ClusterClassifier
from prediction.predict_friendship import FriendshipTrainer, combine_balanced_friends
from prediction_tools import init_indexes, stream_csv

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate trust link predictions.')
    parser.add_argument('single',  type=str,
                        help='Path to single-agent feature csv.')
    parser.add_argument('pairwise', type=str,
                        help='Path to pairwise-agent feature csv.')
    parser.add_argument('output',  type=str,
                        help="Path to a file where output clusters are written.")
    parser.add_argument('target', type=str, choices={'pcc', 'friend', 'realfriend'},
                        help="The target of prediction.")
    parser.add_argument('--clusters', dest='clusters', type=str, nargs=1,
                        help="Path to cluster assignments. If empty, assume one global cluster.")
    args = parser.parse_args()

    single_path = args.single
    pairwise_path = args.pairwise
    output_path = args.output
    cluster_path = args.clusters

    init_indexes(single_path, pairwise_path)

    # Assign all users to a single cluster if no clusters are specified
def run():
    if cluster_path is not None:
        with open(cluster_path) as f:
            clusters = json.load(f)
    else:
        clusters = []
        for line in stream_csv(single_path):
            clusters.append(0)

    classifier = ClusterClassifier(clusters)
    classifier.SINGLE_PATH = single_path
    classifier.PAIRWISE_PATH = pairwise_path
    classifier.fit(combine_balanced_friends, FriendshipTrainer())



