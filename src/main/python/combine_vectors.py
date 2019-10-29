#!/usr/bin/python3
import sys
from tqdm import tqdm
from prediction_tools import *


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
    t = tqdm(total=num_vects)
    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            if test(pair):
                vects.append(build_vect(pair, singleById))
                posCount += 1
                t.update()
            elif negCount < posCount:
                vects.append(build_vect(pair, singleById))
                negCount += 1
                t.update()
            if len(vects) >= num_vects:
                break
    t.close()
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
            if (not test(pair)) and not(negCount < posCount):
                continue
            if not (pair[0] in userIds or pair[1] in userIds):
                continue
            if test(pair):
                vects.append(build_vect(pair, singleById))
                posCount += 1
            elif negCount < posCount:
                vects.append(build_vect(pair, singleById))
                negCount += 1
        return vects


def combine_stream(singlePath, pairPath):
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            yield build_vect(pair, singleById)


if __name__ == '__main__':
    singlePath, pairPath, outputPath = sys.argv[1:]
    combined = _combine_balanced_num(singlePath, pairPath, 100_000)
