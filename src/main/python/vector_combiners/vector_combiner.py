import csv
from itertools import chain

import numpy as np


class VectorCombiner:
    def __init__(self, single_path, pairwise_path):
        self.single_path = single_path
        self.pairwise_path = pairwise_path
        self.single_header = None
        self.pairwise_header = None
        self.header = None
        self._parse_headers()

    def _parse_headers(self):
        with open(self.single_path, 'r') as f:
            self.single_header = [s.strip() for s in f.readline().split(',')]
            single_header = self.single_header[1:]
        with open(self.pairwise_path, 'r') as f:
            self.pairwise_header = [s.strip() for s in f.readline().split(',')]
            pairwise_header = self.pairwise_header[2:]

        self.header = ['user1_id', 'user2_id'] + ['user1_' + s for s in single_header] + \
               ['user2_' + s for s in single_header] + pairwise_header

    def array(self, length, **kwargs):
        """Create a numpy array comprised of `length` combined vectors, includes user ids"""
        arr = np.ndarray((length, len(self.header)), dtype=np.float16)
        generator = self.stream(**kwargs)
        for i in range(length):
            arr[i] = next(generator)
        return arr

    def stream(self, **kwargs):
        """Stream vectors, including user ids."""
        single_vects_by_id = self._single_vects_by_id()
        for line in self.stream_csv(self.pairwise_path):
            yield self._combine_vects(line, single_vects_by_id)

    def _single_vects_by_id(self):
        return {line[0]: line[1:] for line in self.stream_csv(self.single_path)}

    @staticmethod
    def stream_csv(path):
        with open(path, 'r') as f:
            _ = f.readline()  # throw away header
            reader = csv.reader(f)
            for line in reader:
                yield line

    def _combine_vects(self, line, single_vects_by_id):
        user1_vect = single_vects_by_id[line[0]]
        user2_vect = single_vects_by_id[line[1]]

        vect = [int(line[0]), int(line[1])]
        for entry in chain(user1_vect, user2_vect, line[2:]):
            if entry == 'null':
                vect.append(0.0)
            else:
                vect.append(float(entry))
        return vect
