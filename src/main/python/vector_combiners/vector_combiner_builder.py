from vector_combiners import TextVectorCombiner
from vector_combiners.binary_vector_combiner import BinaryVectorCombiner


class VectorCombinerBuilder:
    @staticmethod
    def build(single_path, pairwise_path):
        if single_path.endswith('.csv'):
            return TextVectorCombiner(single_path, pairwise_path)
        else:
            return BinaryVectorCombiner(single_path, pairwise_path)