#!/usr/bin/python3

from data_set import DataSet
from regression import learn_logit
from sklearn.model_selection import train_test_split
from combine_vectors import *


def learn_classifier(lines, header, train_size):
    ds = DataSet(lines, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    #ds = ds.scale()

    print("Learning")
    if train_size < 1.0:
        X, X_test, Y, Y_test = train_test_split(ds.X, ds.Y, train_size=0.8, shuffle=True, random_state=42)
        clf = learn_logit(X, Y)
        return clf, clf.score(X_test, Y_test)
    else:
        clf = learn_logit(ds.X, ds.Y)
        return clf, clf.score(ds.X, ds.Y)


def output_predictions(single_path, pairwise_path, output_path, classifier):
    def pairfilter(pair):
        pair[INDEXES['areFriends']] = 0
        return pair[2:]

    stream = combine_stream(single_path, pairwise_path)
    write_predictions(stream, pairfilter, classifier, output_path)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_friendship.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    print("Loading data")
    header = combined_headers(single_path, pairwise_path)
    combined = combine_balanced_friends(single_path, pairwise_path, 500_000)
    clf = learn_classifier(combined, header, 0.8)

    def pairfilter(pair):
        pair[INDEXES['areFriends']] = 0
        return pair[2:]

    stream = combine_stream(single_path, pairwise_path)
    write_predictions(stream, pairfilter, clf, output_path)

