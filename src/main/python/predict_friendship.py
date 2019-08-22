#!/usr/bin/python3

from data_set import DataSet
from regression import learn_logit
from sklearn.model_selection import train_test_split
from combine_vectors import *


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_friendship.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    os.path.dirname(output_path)
    header = combined_headers(single_path, pairwise_path)
    combined = combine_balanced_num(single_path, pairwise_path, 200_000)

    ds = DataSet(combined, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    ds = ds.scale()

    X, X_test, Y, Y_test = train_test_split(ds.X, ds.Y, train_size=150_000, shuffle=True, random_state=42)
    clf = learn_logit(X, Y)
    print(clf.score(X_test, Y_test))

    #write_predictions(ds, clf, output_path)
