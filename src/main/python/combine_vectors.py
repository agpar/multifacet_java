#!/usr/bin/python3
import csv
from predict_friendship import *
from tqdm import tqdm


def combine(singlePath, pairPath, outupt_path):
    with open(singlePath, 'r') as f:
        headerSingle = [h.strip() for h in f.readline().split(',')][1:]
        reader = csv.reader(f)
        singleById = {line[0]: line[1:] for line in reader}

    def build_vect(pair):
        user1_vect = singleById[pair[0]]
        user2_vect = singleById[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        return pair[:2] + parse_pairwise_line(vect)

    strangerCount, friendCount = 0, 0
    with open(outputPath, 'w') as fout:
        writer = csv.writer(fout)
        with open(pairPath, 'r') as fin:
            headerPair = [h.strip() for h in fin.readline().split(',')][1:]
            header = (["user1_id", "user2_id"] + 
                      ["user1_" + h for h in headerSingle] +
                      ["user2_" + h for h in headerSingle] + 
                      headerPair)
            writer.writerow(header)

            reader = csv.reader(fin)
            for pair in tqdm(reader):
                if int(pair[FRIEND_IND]):
                    writer.writerow(build_vect(pair))
                    friendCount += 1
                elif strangerCount < friendCount:
                    writer.writerow(build_vect(pair))
                    strangerCount += 1


if __name__ == '__main__':
    singlePath, pairPath, outputPath = sys.argv[1:]
    combine(singlePath, pairPath, outputPath)
