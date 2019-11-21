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


def stream_csv(path):
    with open(path, 'r') as f:
        header = f.readline()
        reader = csv.reader(f)
        for line in reader:
            yield line


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
    INDEXES['PCC'] = full_header.index("PCC") - len(full_header) 
    INDEXES['areFriends'] = full_header.index("areFriends") - len(full_header)
    INDEXES['socialJacc'] = full_header.index("socialJacc") - len(full_header)


def write_predictions(stream, clf, path):
    with open(path, 'w') as f:
        for pair in stream:
            user1_id = int(pair[0])
            user2_id = int(pair[1])
            pred1 = int(clf.predict(user1_id, [clf.trainer.filter_target(pair)])[0])
            pred2 = int(clf.predict(user2_id, [clf.trainer.filter_target(pair)])[0])
            if pred1:
                f.write(f"{user1_id} {user2_id} {pred1}\n")
            if pred2:
                f.write(f"{user2_id} {user1_id} {pred2}\n")
