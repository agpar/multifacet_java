import numpy as np

from vector_combiners.vector_combiner import VectorCombiner


class BinaryVectorCombiner(VectorCombiner):
    def __init__(self, single_path, pairwise_path):
        super().__init__()
        self._validate_paths(single_path, pairwise_path)
        self.single_path = single_path
        self.pairwise_path = pairwise_path
        self.single_header = self._parse_header(self.single_path)
        self.pairwise_header = self._parse_header(self.pairwise_path)
        self.header = self._combine_headers()
        self._single_vects = self._load_single_vects_by_id()

    def _validate_paths(self, single_path, pairwise_path):
        for path in [single_path, pairwise_path]:
            if not path.endswith('.npz'):
                raise Exception(f"BinaryVectorCombiner passed non .npz file: {path}")

    def _parse_header(self, path):
        concated_data = np.load(path)
        return list(concated_data['header'])

    def _combine_headers(self):
        header = ['user1_id', 'user2_id']
        header.extend([f'user1_{s}' for s in self.single_header[1:]])
        header.extend([f'user2_{s}' for s in self.single_header[1:]])
        header.extend(self.pairwise_header[2:])
        return header

    def _load_single_vects_by_id(self):
        compressed_data = np.load(self.single_path)
        ids, data = compressed_data['ids'], compressed_data['data']
        # Assert ids are sorted
        assert(False not in (np.diff(ids)>=0))
        return data

    def user_count(self):
        return len(self._single_vects)

    def stream(self, user_ids=None, mmap_mode='r', **kwargs):
        try:
            compressed_pairwise = np.load(self.pairwise_path, mmap_mode=mmap_mode)
            if user_ids:
                pair_ids, pair_data = self._filter_users(compressed_pairwise, user_ids)
            else:
                pair_ids, pair_data = compressed_pairwise['ids'], compressed_pairwise['data']

            for ids, data in zip(pair_ids, pair_data):
                id1, id2 = ids
                yield ids, self._combine_vects(id1, id2, data)
        finally:
            del compressed_pairwise

    def _filter_users(self, compressed_pairwise, user_ids):
        pair_ids = compressed_pairwise['ids']
        first_ids = pair_ids[:,0]
        interested_indexes = np.where(np.isin(first_ids, list(user_ids)))
        return pair_ids[interested_indexes], compressed_pairwise['data'][interested_indexes]

    def _combine_vects(self, id1, id2, pairwise_vect):
        new_arr = np.empty(len(self.header) - 2, dtype=np.float32)
        new_arr[0: self._single_vects.shape[1]] = self._single_vects[id1]
        new_arr[self._single_vects.shape[1]: 2*self._single_vects.shape[1]] = self._single_vects[id2]
        new_arr[2*self._single_vects.shape[1]:] = pairwise_vect
        return new_arr

