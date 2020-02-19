from prediction.classifier_trainer import ClassifierTrainer
from data_set import DataSet


class FriendPredictor:
    def __init__(self, header):
        self.friend_index = header.index('areFriends')

    def predict(self, pairs):
        predictions = []
        for pair in pairs:
            predictions.append(int(pair[self.friend_index]))
        return predictions


class RealFriendTrainer(ClassifierTrainer):
    def learn_classifier(self, X, Y,  train_size):
        return FriendPredictor(self.header), 1.0

    def filter_target(self, line):
        return line

    def to_dataset(self, lines, header):
        lines_mem = []
        for i in range(10_000):
            lines_mem.append(next(lines))
        ds = DataSet(lines_mem, header)
        ds = ds.split(header.index('areFriends'), start_col=2)
        return ds.X, ds.Y
