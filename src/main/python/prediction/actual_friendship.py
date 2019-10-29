from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import INDEXES
from combine_vectors import combine_stream, write_predictions


class FriendPredictor:
    def predict(self, pairs):
        predictions = []
        for pair in pairs:
            predictions.append(int(pair[INDEXES['areFriends']]))
        return predictions


class RealFriendTrainer(ClassifierTrainer):
    def learn_classifier(self, lines, header, train_size):
        return FriendPredictor(), 1.0

    def output_predictions(self, single_path, pairwise_path, output_path, classifier):
        stream = combine_stream(single_path, pairwise_path)
        write_predictions(stream, self.filter_target, classifier, output_path)

    @staticmethod
    def filter_target(line):
        return line[2:]
