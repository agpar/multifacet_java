from vector_combiners.vector_combiner import VectorCombiner
import numpy as np


class BalancedDataSetBuilder(VectorCombiner):
    def __init__(self, vector_combiner: VectorCombiner, balancer_fn=None):
        super().__init__()
        self.combiner = vector_combiner
        self.header = vector_combiner.header
        self.pairwise_header = vector_combiner.pairwise_header
        self.single_header = vector_combiner.single_header

        if balancer_fn is None:
            balancer_fn = lambda x: True
        self._balancer = balancer_fn

    def stream(self, user_ids=None, max_lines=None):
        """Stream vectors, optionally filtering by user_ids and using a max.

        user_ids: If not None, a set of user_ids we are interested in. Only pairwise vectors
        where user_ids in the first position are in this set are considered.
        max_lines: If not None, the maximum amount of lines to stream.
        """
        vector_stream = self.combiner.stream(user_ids=user_ids, max_lines=max_lines)
        if user_ids is not None:
            vector_stream = self._filter_user_ids(vector_stream, user_ids)
        vector_stream = self._filter_balance(vector_stream)
        if max_lines is not None:
            vector_stream = self._filter_max(vector_stream, max_lines)
        yield from vector_stream

    def _filter_user_ids(self, lines, user_ids):
        for line in lines:
            if int(line[0]) in user_ids:
                yield line

    def _filter_balance(self, lines):
        neg_count = 0
        pos_count = 0
        for line in lines:
            balance_result = self._balancer(line)
            if balance_result is None:
                continue
            if balance_result:
                yield line
                pos_count += 1
            elif neg_count < pos_count:
                yield line
                neg_count += 1

    def _filter_max(self, lines, max_lines):
        counter = 0
        for line in lines:
            yield line
            counter += 1
            if counter >= max_lines:
                break


class BalancedFriendDataSetBuilder(BalancedDataSetBuilder):
    def __init__(self, vector_combiner):
        super().__init__(vector_combiner)

        friend_index = self.combiner.header.index('areFriends')
        def are_friends(line):
            return int(line[friend_index]) > 0
        self._balancer = are_friends


class BalancedPCCDataSetBuilder(BalancedDataSetBuilder):
    def __init__(self, vector_combiner):
        super().__init__(vector_combiner)

        pcc_index = self.combiner.header.index('PCC')
        def pos_pcc(line):
            pcc = line[pcc_index]
            if pcc == 'null' or np.isnan(pcc):
                return None
            return pcc > 0
        self._balancer = pos_pcc

