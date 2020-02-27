
class VectorCombiner:
    def __init__(self):
        self.header = None
        self.pairwise_header = None
        self.single_header = None

    def stream(self, **kwargs):
        raise NotImplementedError

    def array(self, length, **kwargs):
        raise NotImplementedError