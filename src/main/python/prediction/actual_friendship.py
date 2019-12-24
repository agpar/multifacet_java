from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import INDEXES


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
        return lines
