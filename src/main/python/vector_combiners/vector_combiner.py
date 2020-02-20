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
        self.header_no_ids = None
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
        self.header_no_ids = self.header[2:]

    def array(self, length, exclude_ids=False, **kwargs):
        """Create a numpy array comprised of `length` combined vectors, includes user ids"""
        col_names = self.header
        if exclude_ids:
            col_names = self.header[2:]

        arr = np.ndarray((length, len(col_names)), dtype=np.float16)
        generator = self.stream(**kwargs)
        if exclude_ids:
            generator = self._filter_out_ids(generator)

        for i in range(length):
            arr[i] = next(generator)
        return arr

    def _filter_out_ids(self, vects):
        for vect in vects:
            yield vect[2:]

    def stream(self, **kwargs):
        """Stream vectors, including user ids."""
        single_vects_by_id = self._single_vects_by_id()
        for line in self.stream_csv(self.pairwise_path):
            yield self._combine_vects(line, single_vects_by_id)

    def _single_vects_by_id(self):
        lines = list(self.stream_csv(self.single_path))
        arr = np.empty((len(lines), len(lines[0]-1)))
        for line in lines:
            arr[int(line[0])] = map(self._to_float, line[1:])
        return arr

    @staticmethod
    def stream_csv(path):
        with open(path, 'r') as f:
            _ = f.readline()  # throw away header
            reader = csv.reader(f)
            for line in reader:
                yield line

    def _combine_vects(self, line, single_vects_by_id):
        user1_id, user2_id = int(line[0]), int(line[1])
        user1_vect = single_vects_by_id[user1_id]
        user2_vect = single_vects_by_id[user2_id]

        vect = [user1_id, user2_id]
        vect.extend(map(self._to_float, chain(user1_vect, user2_vect, line[2:])))
        return vect

    def _to_float(self, str_val):
        if str_val == 'null':
            return 0.0
        return float(str_val)
