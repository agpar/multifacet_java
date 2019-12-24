from data_set import *
from prediction_tools import INDEXES

from prediction.classifier_trainer import ClassifierTrainer


class PCCTrainer(ClassifierTrainer):

    @staticmethod
    def to_dataset(lines, header):
        ds = DataSet(lines, header)
        ds = ds.split(header.index('PCC'), start_col=2)

        Y_disc = [1 if y > 0 else 0 for y in ds.Y]
        X, Y = [], []
        for x, y in zip(ds.X, Y_disc):
            if y is not None:
                X.append(x)
                Y.append(y)
        return X, Y

    @staticmethod
    def filter_target(line):
        line[INDEXES['PCC']] = 0
        return line
