#!/usr/bin/python3
import csv
import os
from tqdm import tqdm
from prediction_tools import *


def get_combined_path(outputPath):
    return os.path.join(os.path.dirname(outputPath), 'combined.csv')


def load_combined(singlePath, pairPath, outputPath):
    if os.path.exists(outputPath):
        return read_csv(outputPath)
    else:
        combine(singlePath, pairPath)
        return read_csv(outputPath)


def combine(singlePath, pairPath, num_vects):
    with open(singlePath, 'r') as f:
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    def build_vect(pair):
        user1_vect = singleById[pair[0]]
        user2_vect = singleById[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        return pair[:2] + parse_pairwise_line(vect)

    strangerCount, friendCount = 0, 0
    vects = []
    with open(pairPath, 'r') as fin:
        reader = csv.reader(fin)
        for pair in tqdm(reader):
            if int(pair[FRIEND_IND]):
                vects.append(build_vect(pair))
                friendCount += 1
            elif strangerCount < friendCount:
                vects.append(build_vect(pair))
                strangerCount += 1
            if len(vects) >= num_vects:
                return vects


if __name__ == '__main__':
    singlePath, pairPath, outputPath = sys.argv[1:]
    combine(singlePath, pairPath, outputPath)
