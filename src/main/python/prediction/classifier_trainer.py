from regression import learn_logit
from sklearn.model_selection import train_test_split
from prediction_tools import *


class ClassifierTrainer:

    def learn_classifier(self, X, Y, header, train_size):
        if train_size < 1.0:
            X, X_test, Y, Y_test = train_test_split(X, Y, train_size=0.8, shuffle=True, random_state=42)
            clf = learn_logit(np.array(X), np.array(Y))
            return clf, clf.score(np.array(X_test), np.array(Y_test))
        else:
            clf = learn_logit(np.array(X), np.array(Y))
            return clf, clf.score(np.array(X), np.array(Y))

    @staticmethod
    def to_dataset(lines, header):
        """Transform lines into (X,Y) by identifying the target variable."""
        raise NotImplemented

    @staticmethod
    def filter_target(line):
        """Remove the target variable from a line."""
        raise NotImplemented

    @staticmethod
    def curry_filter(index):
        def filter_target(line):
            line[index] = 0
            return line
        return filter_target