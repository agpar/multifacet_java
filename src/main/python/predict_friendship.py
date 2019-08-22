#!/usr/bin/python3


import sys
import csv
from data_set import DataSet
from regression import learn_logit
from sklearn.model_selection import train_test_split
from prediction_tools import *
from combine_vectors import *


def write_predictions(ds_all, clf, path):
    predictions = [x for x in clf.predict(ds_all.X)]
    with open(path, 'w') as f:
        for data_line, prediction in zip(ds_all.data, predictions):
            if prediction != 1:
                continue
            user1_id = data_line[0]
            user2_id = data_line[1]
            f.write(f"{user1_id} {user2_id} {prediction}\n")
            f.write(f"{user2_id} {user1_id} {prediction}\n")


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_friendship.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    os.path.dirname(output_path)
    combined = load_combined(single_path, pairwise_path, get_combined_path(outputPath))
    header = combined_headers(single_path, pairwise_path)

    ds = DataSet(combined, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    ds = ds.scale()

    X, Y, X_test, Y_test = train_test_split(ds.X, ds.Y, train_size=1_000_000, shuffle=True, random_state=42)
    clf = learn_logit(X, Y)
    print(clf.score(X_test, Y_test))

    #write_predictions(ds, clf, output_path)
