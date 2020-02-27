import numpy as np


class CombinedVectorLoader:
    def __init__(self, npz_file_path):
        self.file_path = npz_file_path

    def load_all(self):
        """Load the id and vector data"""
        data = np.load(self.file_path)
        return data['ids'], data['vects']

    def load_for(self, user_ids):
        """Load only the vectors where the first user_id is in user_ids"""
        data = np.load(self.file_path, mmap_mode='r')
        id_indexes = []
        for i, user1_id, user2_id in enumerate(data['ids']):
            if user1_id in user_ids:
                id_indexes.append(i)

        if len(id_indexes) == 0:
            return np.array([])
        else:
            return np.copy(data[id_indexes])
