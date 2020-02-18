from data_set import DataSet
from prediction.classifier_trainer import ClassifierTrainer


class FriendshipTrainer(ClassifierTrainer):

    def __init__(self, header):
        super().__init__(header)
        self.friend_index = header.index('areFriends')

    def to_dataset(self, lines, header):
        ds = DataSet(lines, header)
        ds = ds.split(self.friend_index, start_col=2)
        return ds.X, ds.Y

    def filter_target(self, line):
        line[self.friend_index] = 0
        return line
