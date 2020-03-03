import csv
import os
from itertools import chain

import numpy as np

from vector_combiners.vector_combiner import VectorCombiner


class TextVectorCombiner(VectorCombiner):
    def __init__(self, single_path, pairwise_path):
        super().__init__()
        self.single_path = single_path
        self.pairwise_path = pairwise_path
        self.single_header = None
        self.pairwise_header = None
        self.header = None
        self._parse_headers()

    def _validate_paths(self, single_path, pairwise_path):
        for path in [single_path, pairwise_path]:
            if not path.endswith('.csv'):
                raise Exception(f"TextVectorCombiner passed non .csv file: {path}")

    def _parse_headers(self):
        with open(self.single_path, 'r') as f:
            self.single_header = [s.strip() for s in f.readline().split(',')]
            single_header = self.single_header[1:]
        with open(self.pairwise_path, 'r') as f:
            self.pairwise_header = [s.strip() for s in f.readline().split(',')]
            pairwise_header = self.pairwise_header[2:]

        self.header = ['user1_id', 'user2_id'] + ['user1_' + s for s in single_header] + \
               ['user2_' + s for s in single_header] + pairwise_header

    def stream(self, **kwargs):
        """Stream vectors, including user ids."""
        single_vects_by_id = self._single_vects_by_id()
        for line in self.stream_csv(self.pairwise_path):
            yield self._combine_vects(line, single_vects_by_id)

    def _single_vects_by_id(self):
        lines = list(self.stream_csv(self.single_path))
        arr = np.empty((len(lines), len(lines[0]) - 1))
        for line in lines:
            arr[int(line[0])] = list(map(self.to_float, line[1:]))
        return arr

    def _combine_vects(self, line, single_vects_by_id):
        user1_id, user2_id = int(line[0]), int(line[1])
        user1_vect = single_vects_by_id[user1_id]
        user2_vect = single_vects_by_id[user2_id]

        vect = list(map(self.to_float, chain(user1_vect, user2_vect, line[2:])))
        return np.array([user1_id, user2_id]), np.array(vect)

    @staticmethod
    def stream_csv(path):
        with open(path, 'r') as f:
            _ = f.readline()  # throw away header
            reader = csv.reader(f)
            for line in reader:
                yield line

    @staticmethod
    def to_float(str_val):
        if str_val == 'null':
            return float('nan')
        return float(str_val)

    def user_count(self):
        return TextVectorCombiner.csv_data_len(self.single_path)

    @staticmethod
    def csv_data_len(path):
        wc_stream = os.popen(f'wc -l {path}')
        return int(wc_stream.read().split(' ')[0]) - 1