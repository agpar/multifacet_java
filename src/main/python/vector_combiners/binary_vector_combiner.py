import numpy as np

from vector_combiners.vector_combiner import VectorCombiner


class BinaryVectorCombiner(VectorCombiner):
    def __init__(self, single_file_npy, pairwise_file_npy):
        super().__init__()
        self.single_file_npz = single_file_npy
        self.pairwise_file_npz = pairwise_file_npy
        self.single_header = self._parse_header(self.single_file_npz)
        self.pairwise_header = self._parse_header(self.pairwise_file_npz)
        self.header = self._combine_headers()
        self._arr_len = None
        self._single_vects = None

    def _parse_header(self, path):
        concated_data = np.load(path)
        return list(concated_data['header'])

    def _combine_headers(self):
        header = ['user1_id', 'user2_id']
        header.extend([f'user1_{s}' for s in self.single_header[1:]])
        header.extend([f'user2_{s}' for s in self.single_header[1:]])
        header.extend(self.pairwise_header[2:])
        return header

    def stream(self, user_ids=None, mmap_mode='r', **kwargs):
        self._single_vects = self._single_vects_by_id()
        try:
            compressed_pairwise = np.load(self.pairwise_file_npz, mmap_mode=mmap_mode)
            pair_ids, pair_data = compressed_pairwise['ids'], compressed_pairwise['data']
            if user_ids:
                pass
                # filter out irrelevant stuff

            self._arr_len = 2 + (2 * self._single_vects.shape[1]) + pair_data.shape[1]
            for ids, data in zip(pair_ids, pair_data):
                id1, id2 = ids
                yield self._combine_vects(id1, id2, data)
        finally:
            del compressed_pairwise

    def array(self, length, **kwargs):
        pass

    def _combine_vects(self, id1, id2, pairwise_vect):
        new_arr = np.empty(self._arr_len, dtype=np.float32)
        new_arr[0] = id1
        new_arr[1] = id2
        new_arr[2: 2 + self._single_vects.shape[1]] = self._single_vects[id1]
        new_arr[2 + self._single_vects.shape[1]: 2 + 2*self._single_vects.shape[1]] = self._single_vects[id2]
        new_arr[2 + 2*self._single_vects.shape[1]:] = pairwise_vect
        return new_arr

    def _single_vects_by_id(self):
        compressed_data = np.load(self.single_file_npz)
        ids, data = compressed_data['ids'], compressed_data['data']
        # Assert ids are sorted
        assert(False not in (np.diff(ids)>=0))
        return data
