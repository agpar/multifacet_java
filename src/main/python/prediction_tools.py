import csv
import numpy as np
from multiprocessing import Queue, Process
import json

INDEXES = {
    'PCC': None,
    'areFriends': None
}


def parse_pairwise_line(line):
    pcc_ind = INDEXES['PCC']
    if line[pcc_ind] == 'null':
        line[pcc_ind] = 0.0
    for i, item in enumerate(line):
        if item == 'null':
            line[i] = 0.0
    return [float(x) for x in line]


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


def build_vect(pair, singleById):
    user1_vect = singleById[pair[0]]
    user2_vect = singleById[pair[1]]
    vect = user1_vect + user2_vect + pair[2:]
    return pair[:2] + parse_pairwise_line(vect)


def pair_are_friends(pair):
    return bool(int(pair[INDEXES['areFriends']]))


def pcc_positive(pair):
    pcc = pair[INDEXES['PCC']]
    if pcc == 'null':
        return False
    return float(pcc) > 0


def combine_balanced_pcc(singlePath, pairPath, numVects=None, userIds=None):
    return _combine_multiplex(singlePath, pairPath, numVects, userIds, pcc_positive)


def combine_balanced_friends(singlePath, pairPath, numVects=None, userIds=None):
    return _combine_multiplex(singlePath, pairPath, numVects=numVects, userIds=userIds, test=pair_are_friends)


def _combine_multiplex(singlePath, pairPath, numVects=None, userIds=None, test=None):
    if numVects is None and userIds is None:
        raise Exception("Must specify either numVects or userIds.")
    if numVects is not None and userIds is not None:
        raise Exception("Only specify one of numVect or userIds.")
    if numVects:
        return _combine_balanced_num(singlePath, pairPath, numVects, test)
    elif userIds:
        return _combine_balanced_ids(singlePath, pairPath, userIds, test)
    else:
        raise Exception("Can't combine vectors.")


def _combine_balanced_num(singlePath, pairPath, num_vects, test=None):
    """Pull in `num_vects` size balanced from singlePath and pairPath"""
    init_indexes(singlePath, pairPath)
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    negCount, posCount = 0, 0
    vects = []
    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            if test(pair):
                vects.append(build_vect(pair, singleById))
                posCount += 1
            elif negCount < posCount:
                vects.append(build_vect(pair, singleById))
                negCount += 1
            if len(vects) >= num_vects:
                break
    return vects


def _combine_balanced_ids(singlePath, pairPath, userIds, test=None):
    """Pull in vects involving `userIds` from singlePath and pairPath"""
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    vects = []
    negCount, posCount = 0, 0
    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            if not int(pair[0]) in userIds:
                continue
            if test(pair):
                vects.append(build_vect(pair, singleById))
                posCount += 1
            elif negCount < posCount:
                vects.append(build_vect(pair, singleById))
                negCount += 1
        return vects


def combine_stream(singlePath, pairPath, **kwargs):
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            yield build_vect(pair, singleById)


def write_predictions(stream, clf, path):
    with open(path, 'w') as f:
        for pair in stream:
            user1_id = int(pair[0])
            user2_id = int(pair[1])
            pred1 = int(clf.predict(user1_id, [clf.trainer.filter_target(pair[2:])])[0])
            if pred1:
                f.write(f"{user1_id} {user2_id} {pred1}\n")


def multiprocess_predict(single_path, pairwise_path, output_path, cluster_classifier):
    actual_classifiers = sum([1 for i in cluster_classifier.classifiers if i is not None]) + 1
    queues = [Queue() for i in range(min(max(actual_classifiers, 16), 16))]
    procs = []
    i = 0
    line_filter = cluster_classifier.trainer.filter_target
    if actual_classifiers > 1:
        for clf in cluster_classifier.classifiers:
            if clf is None:
                continue
            args = (queues[i], (single_path, pairwise_path, output_path), line_filter, clf)
            p = Process(target=runner, args=args)
            procs.append(p)
            p.start()
            i += 1
    else:
        for i in range(15):
            args = (queues[i], (single_path, pairwise_path, output_path), line_filter, cluster_classifier.overall_classifier)
            p = Process(target=runner, args=args)
            procs.append(p)
            p.start()

    args = (queues[-1], (single_path, pairwise_path, output_path), line_filter, cluster_classifier.overall_classifier)
    p = Process(target=runner, args=args)
    procs.append(p)
    p.start()

    tot = 0
    for line in combine_stream(single_path, pairwise_path):
        user1_id = int(line[0])
        user2_id = int(line[1])
        user1_cluster_idx = cluster_classifier.user_clusters[user1_id]
        if cluster_classifier.classifiers[user1_cluster_idx] is None:
            user1_cluster_idx = len(queues) - 1
        user2_cluster_idx = cluster_classifier.user_clusters[user2_id]
        if cluster_classifier.classifiers[user2_cluster_idx] is None:
            user2_cluster_idx = len(queues) - 1
        if actual_classifiers > 1:
            queues[user1_cluster_idx].put((user1_id, user2_id, np.array(line[2:])), block=False)
            queues[user2_cluster_idx].put((user2_id, user1_id, np.array(line[2:])), block=False)
        else:
            queues[int(tot/2) % 15].put((user1_id, user2_id, np.array(line[2:])), block=False)
            queues[int(tot/2) % 15].put((user2_id, user1_id, np.array(line[2:])), block=False)
        tot += 2
        if tot % 100 == 0:
            print(tot)

    for queue in queues:
        # This needs to be fixed for the case when multiple workers are reading from same queue.
        queue.put(None)

    for p in procs:
        p.join()


def write_buffer(line_buffer, clf, target_filter, output_path):
    predictions = clf.predict(np.array([target_filter(pair[2]) for pair in line_buffer]))
    lines = []
    for buf_line, prediction in zip(line_buffer, predictions):
        if prediction:
            truster, trustee, _ = buf_line
            lines.append(f"{truster} {trustee} {prediction}\n")
    with open(output_path, 'a') as f:
        f.writelines(lines)


def runner(queue: Queue, paths, target_filter, clf):
    single_path, pairwise_path, output_path = paths
    init_indexes(single_path, pairwise_path)
    BUFFER_LEN = 10_000
    line_buffer = []
    val = queue.get()
    while val is not None:
        line_buffer.append(val)
        if len(line_buffer) > BUFFER_LEN:
            write_buffer(line_buffer, clf, target_filter, output_path)
            line_buffer = []
        val = queue.get()
    write_buffer(line_buffer, clf, target_filter, output_path)


def run_predict_proc(single_path, pairwise_path, output_path, target_filter, clf, cluster):
    BUFFER_LEN = 10_000
    line_buffer = []
    with open(output_path, 'a') as f:
        for line in combine_stream(single_path, pairwise_path):
            user1_id = int(line[0])
            user2_id = int(line[1])
            if user1_id in cluster:
                line_buffer.append((user1_id, user2_id, line))
            if user2_id in cluster:
                line_buffer.append((user2_id, user1_id, line))

            if len(line_buffer) > BUFFER_LEN:
                # do predictions and write out the buffer
                predictions = clf.predict(np.array([target_filter(pair[2]) for pair in line_buffer]))
                for buf_line, prediction in zip(line_buffer, predictions):
                    truster, trustee, _ = buf_line
                    f.write(f"{truster} {trustee} {prediction}\n")
                line_buffer = []