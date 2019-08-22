#!/usr/bin/python3

from data_set import *
from combine_vectors import *
from sklearn.model_selection import train_test_split
from regression import learn_logit

import sys

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_pcc.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    header = combined_headers(single_path, pairwise_path)
    combined = combine_balanced_num(single_path, pairwise_path, 200_000)

    ds = DataSet(combined, header)
    ds = ds.split(header.index('PCC'), start_col=2)
    ds = ds.scale()

    Y_disc = [1 if y > 0 else 0 for y in ds.Y]
    X, Y = [], []
    for x, y in zip(ds.X, Y_disc):
        if y is not None:
            X.append(x)
            Y.append(y)

    X, X_test, Y, Y_test = train_test_split(X, Y, train_size=150_000, shuffle=True, random_state=42)
    clf = learn_logit(X, Y)
    print(clf.score(X_test, Y_test))

    write_predictions(ds, clf, output_path)
