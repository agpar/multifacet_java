#!/usr/bin/python3

from data_set import *
from combine_vectors import *
from sklearn.model_selection import train_test_split
from regression import learn_logit

import sys


def learn_classifier(lines, header, train_size):
    ds = DataSet(lines, header)
    ds = ds.split(header.index('PCC'), start_col=2)
    #ds = ds.scale()

    Y_disc = [1 if y > 0 else 0 for y in ds.Y]
    X, Y = [], []
    for x, y in zip(ds.X, Y_disc):
        if y is not None:
            X.append(x)
            Y.append(y)

    if train_size < 1.0:
        X, X_test, Y, Y_test = train_test_split(X, Y, train_size=train_size, shuffle=True, random_state=42)
        clf = learn_logit(X, Y)
        return clf, clf.score(X_test, Y_test)
    else:
        clf = learn_logit(X, Y)
        return clf, clf.score(X, Y)


def output_predictions(single_path, pairwise_path, output_path, classifier):
    def pairfilter(pair):
        pair[INDEXES['PCC']] = 0
        return pair[2:]

    stream = combine_stream(single_path, pairwise_path)
    write_predictions(stream, pairfilter, classifier, output_path)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_pcc.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    header = combined_headers(single_path, pairwise_path)
    combined = combine_balanced_num(single_path, pairwise_path, 500_000)
    clf = learn_classifier(combined, header, 0.8)
    output_predictions(single_path, pairwise_path, output_path, clf)

