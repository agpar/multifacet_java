import csv
import os
from itertools import chain
from tqdm import tqdm
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

    def array(self, length, **kwargs):
        """Create a numpy array comprised of `length` combined vectors, and a map from user ids to indexes"""
        col_names = self.header[2:]

        arr = np.empty((length, len(col_names)), dtype=np.float16)
        id_map = np.empty((length, 2), dtype=np.int32)

        generator = self.stream(**kwargs)
        if 'print_progress' in kwargs and kwargs['print_progress']:
            generator = self._print_progress(generator, length)
        i = 0
        for val in generator:
            id_map[i] = val[:2]
            arr[i] = val[2:]

            i += 1
            if i >= length:
                break

        return id_map, arr

    def _print_progress(self, vects, length):
        t = tqdm(total=length)
        for v in vects:
            yield v
            t.update()

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
        arr = np.empty((len(lines), len(lines[0]) - 1))
        for line in lines:
            arr[int(line[0])] = list(map(self._to_float, line[1:]))
        return arr

    @staticmethod
    def stream_csv(path):
        with open(path, 'r') as f:
            _ = f.readline()  # throw away header
            reader = csv.reader(f)
            for line in reader:
                yield line

    @staticmethod
    def csv_data_len(path):
        wc_stream = os.popen(f'wc -l {path}')
        return int(wc_stream.read().split(' ')[0]) - 1

    def _combine_vects(self, line, single_vects_by_id):
        user1_id, user2_id = int(line[0]), int(line[1])
        user1_vect = single_vects_by_id[user1_id]
        user2_vect = single_vects_by_id[user2_id]

        vect = [user1_id, user2_id]
        vect.extend(map(self._to_float, chain(user1_vect, user2_vect, line[2:])))
        return vect

    def _to_float(self, str_val):
        if str_val == 'null':
            return float('nan')
        return float(str_val)
