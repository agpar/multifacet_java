#!/usr/bin/python3
import sys
from tqdm import tqdm
from prediction_tools import *


def build_vect(pair, singleById):
    user1_vect = singleById[pair[0]]
    user2_vect = singleById[pair[1]]
    vect = user1_vect + user2_vect + pair[2:]
    return pair[:2] + parse_pairwise_line(vect)


def combine_balanced_num(singlePath, pairPath, num_vects):
    """Pull in `num_vects` size balanced from singlePath and pairPath"""
    init_indexes(singlePath, pairPath)
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    strangerCount, friendCount = 0, 0
    vects = []
    t = tqdm(total=num_vects)
    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            if int(pair[INDEXES['areFriends']]):
                vects.append(build_vect(pair, singleById))
                friendCount += 1
                t.update()
            elif strangerCount < friendCount:
                vects.append(build_vect(pair, singleById))
                strangerCount += 1
                t.update()
            if len(vects) >= num_vects:
                break
    t.close()
    return vects


def combine_balanced_ids(singlePath, pairPath, userIds):
    """Pull in vects involving `userIds` from singlePath and pairPath"""
    with open(singlePath, 'r') as f:
        f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    vects = []
    with open(pairPath, 'r') as fin:
        fin.readline()
        reader = csv.reader(fin)
        for pair in reader:
            if pair[0] in userIds or pair[1] in userIds:
                vects.append(build_vect(pair, singleById))
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
    combined = combine_balanced_num(singlePath, pairPath, 100_000)
