from data_set import DataSet
from prediction.classifier_trainer import ClassifierTrainer
import numpy as np


class FriendshipTrainer(ClassifierTrainer):

    def __init__(self, header):
        super().__init__(header)
        self.friend_index = header.index('areFriends') - 2

    def to_dataset(self, data):
        ds = DataSet(data, self.header)
        ds = ds.split(self.friend_index)
        return ds.X, ds.Y

    def filter_target(self, line):
        ids, vect = line
        vect[self.friend_index] = 0
        np.nan_to_num(vect, copy=False)
        return ids, vect

    def __str__(self):
        return "FriendshipTrainer"
