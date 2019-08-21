#!/usr/bin/python3


import sys
import csv
from data_set import DataSet
from regression import learn_logit
from sklearn.model_selection import train_test_split

PCC_IND = -6
FIEND_IND = -3


def parse_pairwise_line(line):
    parsed = []
    if line[PCC_IND] == 'null':
        line[PCC_IND] = 0.0
    return [float(x) for x in line]


def combined_vectors_balanced(pairwise_vects, solo_vects):
    solo_by_id = {v[0]: v[1:] for v in solo_vects}

    def build_vect(pair):
        user1_vect = solo_by_id[pair[0]]
        user2_vect = solo_by_id[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        return pair[:2] + parse_pairwise_line(vect)

    stranger_vects, friend_vects = [], []
    for pair in pairwise_vects:
        if pair[FIEND_IND]:
            friend_vects.append(build_vect(pair))
        elif len(stranger_vects) < len(friend_vects):
            stranger_vects.append(build_vect(pair))
    return friend_vects + stranger_vects


def combined_headers(single_path, pairwise_path):
    singleHeader = read_csv_header(single_path)[1:]
    user1Headers = ["user1_" + s for s in singleHeader]
    user2Headers = ["user2_" + s for s in singleHeader]
    pairwiseHeader = read_csv_header(pairwise_path)
    return pairwiseHeader[:2] + user1Headers + user2Headers + pairwiseHeader[2:]


def read_csv(path):
    with open(path, 'r') as f:
        header = f.readline()
        reader = csv.reader(f)
        return list(reader)


def read_csv_header(path):
    with open(path, 'r') as f:
        header = f.readline().replace("\n", "")
        return header.split(",")


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
    singles = read_csv(single_path)
    pairwise = read_csv(pairwise_path)
    combined = combined_vectors_balanced(pairwise, singles)
    header = combined_headers(single_path, pairwise_path)

    ds = DataSet(combined, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    ds = ds.scale()

    X, Y, X_test, Y_test = train_test_split(ds.X, ds.Y, train_size=1_000_000, shuffle=True, random_state=42)
    clf = learn_logit(X, Y)
    print(clf.score(X_test, Y_test))

    write_predictions(ds, clf, output_path)