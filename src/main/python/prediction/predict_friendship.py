from data_set import DataSet
from prediction.classifier_trainer import ClassifierTrainer
from prediction_tools import *


class FriendshipTrainer(ClassifierTrainer):

    @staticmethod
    def to_dataset(lines, header):
        ds = DataSet(lines, header)
        ds = ds.split(header.index('areFriends'), start_col=2)
        return ds.X, ds.Y

    @staticmethod
    def filter_target(line):
        line[INDEXES['areFriends']] = 0
        return line
