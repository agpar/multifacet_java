#!/usr/bin/python3

from predict_friendship import *
import sys

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
    ds = ds.split(header.index('PCC'), start_col=2)
    ds = ds.scale()

    Y_disc = [1 if y > 0 else 0 for y in ds.Y]
    X, Y = [], []
    for x, y in zip(ds.X, Y_disc):
        if y is not None:
            X.append(x)
            Y.append(y)


    clf = learn_logit(X, Y)

    print(f"Classifier accuracy: {clf.score(X, Y)}")

    all_combined = combined_vectors_all(pairwise, singles)
    ds_all = DataSet(all_combined, header)
    ds_all = ds_all.split(header.index('PCC'), start_col=2)
    ds_all = ds_all.scale()
    write_predictions(ds_all, clf, output_path)
