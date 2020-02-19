import argparse
import json

from prediction.actual_friendship import RealFriendTrainer
from prediction.cluster_classifier import ClusterClassifier
from prediction.predict_friendship import FriendshipTrainer
from prediction.predict_pcc import PCCTrainer
from vector_combiners import *


def write_predictions(stream, clf, path):
    with open(path, 'w') as f:
        for pair in stream:
            user1_id = int(pair[0])
            user2_id = int(pair[1])
            pred1 = int(clf.predict(user1_id, [clf.trainer.filter_target(pair[2:])])[0])
            if pred1:
                f.write(f"{user1_id} {user2_id} {pred1}\n")


def run(single_path, pairwise_path, output_path, cluster_path, target):
    # Assign all users to a single cluster if no clusters are specified
    if cluster_path is not None:
        with open(cluster_path[0]) as f:
            clusters = json.load(f)
    else:
        clusters = []
        for _ in VectorCombiner.stream_csv(single_path):
            clusters.append(0)

    vc = VectorCombiner(single_path, pairwise_path)

    if target == 'pcc':
        classifier = ClusterClassifier(clusters, BalancedPCCCombiner, PCCTrainer(vc.header))
    elif target == 'friend':
        classifier = ClusterClassifier(clusters, BalancedFriendCombiner, FriendshipTrainer(vc.header))
    elif target == 'realfriend':
        classifier = ClusterClassifier(clusters, BalancedVectorCombiner, RealFriendTrainer(vc.header))
    else:
        print(f"Unknown target of prediction: {target}")
        exit(1)

    classifier.SINGLE_PATH = single_path
    classifier.PAIRWISE_PATH = pairwise_path
    classifier.fit()

    vector_stream = map(classifier.trainer.filter_target, VectorCombiner(single_path, pairwise_path).stream())
    write_predictions(vector_stream, classifier, output_path)


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
