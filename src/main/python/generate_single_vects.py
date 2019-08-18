#!/usr/bin/python3
import sys
import csv
from yelp_interface.trust_indicators import YelpTrustIndicators
from yelp_interface.data_interface import read_data


def write_vectors(yti, X, file):
    labels = yti.solo_vector_labels()
    header = ",".join(labels)
    file.write(f"{header}\n")
    writer = csv.writer(file, delimiter=',')
    for x in X:
        writer.writerow(x)


if __name__ == '__main__':
    """Expects 3 arguments, user_start, user_end, output file."""
    if len(sys.argv) < 4:
        print("Usage: generate_single_vects.py {user_start_idx} {user_end_idx} {output_path}")
        exit(1)
    user_start = int(sys.argv[1])
    user_end = int(sys.argv[2])
    output_path = sys.argv[3]
    yd = read_data(user_range=(user_start, user_end), read_sample=False)
    yti = YelpTrustIndicators(yd)
    X = yti.single_indicators()
    with open(output_path, 'w') as f:
        write_vectors(yti, X, f)
