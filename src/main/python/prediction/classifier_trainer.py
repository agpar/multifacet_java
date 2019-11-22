from regression import learn_logit
from sklearn.model_selection import train_test_split
from prediction_tools import *


class ClassifierTrainer:

    def learn_classifier(self, lines, header, train_size):
        X, Y = self.to_dataset(lines, header)
        print("Learning")
        if train_size < 1.0:
            X, X_test, Y, Y_test = train_test_split(X, Y, train_size=0.8, shuffle=True, random_state=42)
            clf = learn_logit(X, Y)
            return clf, clf.score(X_test, Y_test)
        else:
            clf = learn_logit(X, Y)
            return clf, clf.score(X, Y)

    def output_predictions(self, single_path, pairwise_path, output_path, classifier):
        stream = combine_stream(single_path, pairwise_path)
        write_predictions(stream, self.filter_target, classifier, output_path)

    @staticmethod
    def to_dataset(lines, header):
        """Transform lines into (X,Y) by identifying the target variable."""
        raise NotImplemented

    @staticmethod
    def filter_target(line):
        """Remove the target variable from a line."""
        raise NotImplemented
