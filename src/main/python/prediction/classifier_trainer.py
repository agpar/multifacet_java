from machine_learning.models import learn_rf
from sklearn.model_selection import train_test_split
import numpy as np

from machine_learning.models import ChosenClassifier


class ClassifierTrainer:

    def __init__(self, header):
        self.header = header

    def learn_classifier(self, X, Y, train_size):
        if train_size < 1.0:
            X, X_test, Y, Y_test = train_test_split(X, Y, train_size=0.8, shuffle=True, random_state=42)
            clf = ChosenClassifier().fit(X, Y)
            return clf, clf.score(np.array(X_test), np.array(Y_test))
        else:
            clf = ChosenClassifier().fit(X, Y)
            return clf, clf.score(np.array(X), np.array(Y))

    def to_dataset(self, lines):
        """Transform lines into (X,Y) by identifying the target variable."""
        raise NotImplemented

    def filter_target(self, line):
        """Remove the target variable from a line."""
        raise NotImplemented

    @staticmethod
    def curry_filter(index):
        def filter_target(line):
            line[index] = 0
            return line
        return filter_target