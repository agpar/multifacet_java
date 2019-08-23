#!/usr/bin/python3

import sys
from prediction_tools import INDEXES, init_indexes
from combine_vectors import combine_stream, write_predictions


class FriendPredictor:
    def predict(self, pairs):
        predictions = []
        for pair in pairs:
            predictions.append(int(pair[INDEXES['areFriends']]))
        return predictions


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: actual_friendship.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]

    def pair_filter(pair):
        return pair[2:]

    init_indexes(single_path, pairwise_path)
    stream = combine_stream(single_path, pairwise_path)
    write_predictions(stream, pair_filter, FriendPredictor(), output_path)

