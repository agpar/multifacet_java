import numpy as np

class VectorCombiner:
    def __init__(self):
        self.header = None
        self.single_header = None
        self.pairwise_header = None

    def user_count(self):
        raise NotImplementedError

    def stream(self, **kwargs):
        raise NotImplementedError

    def array(self, **kwargs):
        vects = []
        ids = []
        gen = self.stream(**kwargs)
        for i, data in enumerate(gen):
            pair_ids, vector = data
            vects.append(vector)
            ids.append(pair_ids)
        return np.array(ids), np.array(vects)
