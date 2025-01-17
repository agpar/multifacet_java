from prediction.classifier_trainer import ClassifierTrainer
from data_set import DataSet


def inf_stream(element):
    while True:
        yield element


class FriendPredictor:
    def __init__(self, friend_index):
        self.friend_index = friend_index

    def predict(self, pairs):
        predictions = []
        for pair in pairs:
            predictions.append(int(pair[self.friend_index]))
        return predictions

    def predict_proba(self, pairs):
        return list(zip(inf_stream(0), self.predict(pairs)))

    @property
    def classes_(self):
        return [0, 1]


class RealFriendTrainer(ClassifierTrainer):
    def __init__(self, header):
        super().__init__(header)
        self.friend_index = self.header.index('areFriends') - 2

    def learn_classifier(self, X, Y,  train_size):
        return FriendPredictor(self.friend_index), 1.0

    def filter_target(self, line):
        return line

    def to_dataset(self, data):
        ds = DataSet(data, self.header)
        ds = ds.split(self.friend_index)
        return ds.X, ds.Y

    def __str__(self):
        return "RealFriendTrainer"