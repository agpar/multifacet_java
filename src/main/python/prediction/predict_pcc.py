from data_set import *

from prediction.classifier_trainer import ClassifierTrainer


class PCCTrainer(ClassifierTrainer):

    def __init__(self, header):
        super().__init__(header)
        self.pcc_index = header.index('PCC')

    def to_dataset(self, lines, header):
        ds = DataSet(lines, header)
        ds = ds.split(self.pcc_index, start_col=2)

        Y_disc = [1 if y > 0 else 0 for y in ds.Y]
        X, Y = [], []
        for x, y in zip(ds.X, Y_disc):
            if y is not None:
                X.append(x)
                Y.append(y)
        return X, Y

    def filter_target(self, line):
        line[self.pcc_index] = 0
        return line
