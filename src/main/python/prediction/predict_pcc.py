from data_set import *

from prediction.classifier_trainer import ClassifierTrainer
import numpy as np


class PCCTrainer(ClassifierTrainer):

    def __init__(self, header):
        super().__init__(header)
        self.pcc_index = header.index('PCC') - 2

    def to_dataset(self, data):
        ds = DataSet(data, self.header)
        ds = ds.split(self.pcc_index)

        Y_disc = [1 if y > 0 else 0 for y in ds.Y]
        X, Y = [], []
        for x, y in zip(ds.X, Y_disc):
            if y is not None:
                X.append(x)
                Y.append(y)
        return X, Y

    def filter_target(self, line):
        ids, vect = line
        vect[self.pcc_index] = 0
        np.nan_to_num(vect, copy=False)
        return ids, vect

    def __str__(self):
        return "PCCTrainer"
