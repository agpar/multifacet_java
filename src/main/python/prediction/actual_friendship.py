from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import INDEXES
from data_set import DataSet


class FriendPredictor:
    def predict(self, pairs):
        predictions = []
        for pair in pairs:
            predictions.append(int(pair[INDEXES['areFriends']]))
        return predictions


class RealFriendTrainer(ClassifierTrainer):
    def learn_classifier(self, X, Y, header, train_size):
        return FriendPredictor(), 1.0

    @staticmethod
    def filter_target(line):
        return line

    @staticmethod
    def to_dataset(lines, header):
        lines_mem = []
        for i in range(10_000):
            lines_mem.append(next(lines))
        ds = DataSet(lines_mem, header)
        ds = ds.split(header.index('areFriends'), start_col=2)
        return ds.X, ds.Y
