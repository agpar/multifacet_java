import csv
from predict_friendship import *


def combine(singlePath, pairPath, outupt_path):
    with open(singlePath, 'r') as f:
        headerSingle = f.readline()
        reader = csv.reader(f)
        singleById = {line[0]: line for line in reader}

    def build_vect(pair):
        user1_vect = singleById[pair[0]]
        user2_vect = singleById[pair[1]]
        vect = user1_vect + user2_vect + pair[2:]
        return pair[:2] + parse_pairwise_line(vect)

    stranger_vects, friend_vects = [], []
    with open(pairPath, 'r') as f:
        headerPair = f.readline()
        reader = csv.reader(f)
        for pair in reader:
            if pair[FIEND_IND]:
                friend_vects.append(build_vect(pair))
            elif len(stranger_vects) < len(friend_vects):
                stranger_vects.append(build_vect(pair))

    with open(output_path, 'w') as f:
        header = ["user1_id", "user2_id"] + ["user1_" + h for h in headerSingle] + ["user2_" + h for h in headerSingle]
        header += headerPair
        writer = csv.writer(f)
        writer.write(header)
        for f in friend_vects:
            writer.write(f)
        for s in stranger_vects:
            writer.write(s)

if __name__ == '__main__':
    singlePath, pairPath, outputPath = sys.argv
    combine(singlePath, pairPath, outputPath)
