from vector_combiners import VectorCombiner
import numpy as np


class CombinedVectorSaver:
    def __init__(self, single_path, pairwise_path):
        self.vc = VectorCombiner(single_path, pairwise_path)

    def save(self, output_path):
        pairwise_length = self.vc.csv_data_len(self.vc.pairwise_path)
        ids, vects = self.vc.array(pairwise_length, print_progress=True)
        np.savez_compressed(output_path, ids=ids, vects=vects)
