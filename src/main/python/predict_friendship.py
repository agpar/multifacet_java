#!/usr/bin/python3


import sys
import csv
from data_set import DataSet
from regression import learn_logit

PCC_IND = 22


def parse_pairwise_line(line):
    parsed = []
    if line[PCC_IND] == 'null':
        line[PCC_IND] = 0.0
    return [float(x) for x in line]


def combined_vectors_with_pcc(pairwise_vects, solo_vects):
    solo_by_id = {v[0]: v[1:] for v in solo_vects}
    combined = []
    for pair in pairwise_vects:
        if pair[PCC_IND] == 'null':
            continue
        user1_vect = solo_by_id[pair[0]]
        user2_vect = solo_by_id[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        combined.append(pair[:2] + parse_pairwise_line(vect))
    return combined


def combined_vectors_all(pairwise_vects, solo_vects):
    solo_by_id = {v[0]: v[1:] for v in solo_vects}
    combined = []
    for pair in pairwise_vects:
        user1_vect = solo_by_id[pair[0]]
        user2_vect = solo_by_id[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        combined.append(pair[:2] + parse_pairwise_line(vect))
    return combined


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


def write_real_friends(ds_all, path):
    with open(path, 'w') as f:
        friend_index = ds_all.labels.index('H_areFriends')
        for data_line in ds_all.data:
            if data_line[friend_index]:
                user1_id = data_line[0]
                user2_id = data_line[1]
                f.write(f"{user1_id} {user2_id} 1.0\n")
                f.write(f"{user2_id} {user1_id} 1.0\n")


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: predict_friendship.py {single_vectors_path} {pairwise_vectors_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    singles = read_csv(single_path)
    pairwise = read_csv(pairwise_path)
    combined_with_pcc = combined_vectors_with_pcc(pairwise, singles)
    header = combined_headers(single_path, pairwise_path)

    ds = DataSet(combined_with_pcc, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    ds = ds.scale()

    clf = learn_logit(ds.X, ds.Y)

    all_combined = combined_vectors_all(pairwise, singles)
    ds_all = DataSet(all_combined, header)
    ds_all = ds_all.split(header.index('areFriends'), start_col=2)
    ds_all = ds_all.scale()
    write_predictions(ds_all, clf, output_path)
    #write_real_friends(ds_all, output_path)



