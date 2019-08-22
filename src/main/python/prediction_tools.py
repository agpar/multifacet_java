import csv

INDEXES = {
    'PCC': None,
    'areFriends': None
}


def parse_pairwise_line(line):
    pcc_ind = INDEXES['PCC']
    if line[pcc_ind] == 'null':
        line[pcc_ind] = 0.0
    return [float(x) for x in line]


def read_csv(path):
    with open(path, 'r') as f:
        header = f.readline()
        reader = csv.reader(f)
        return list(reader)


def read_csv_header(path):
    with open(path, 'r') as f:
        header = f.readline().replace("\n", "")
        return header.split(",")


def combined_headers(single_path, pairwise_path):
    singleHeader = read_csv_header(single_path)[1:]
    user1Headers = ["user1_" + s for s in singleHeader]
    user2Headers = ["user2_" + s for s in singleHeader]
    pairwiseHeader = read_csv_header(pairwise_path)
    return pairwiseHeader[:2] + user1Headers + user2Headers + pairwiseHeader[2:]


def init_indexes(single_path, pairwise_path):
    full_header = combined_headers(single_path, pairwise_path)
    INDEXES['PCC'] = len(full_header) - (full_header.index("PCC") + 2)
    INDEXES['areFriends'] = len(full_header) - (full_header.index("areFriends") + 2)


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

