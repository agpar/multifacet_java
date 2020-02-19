from vector_combiners.vector_combiner import VectorCombiner


class BalancedVectorCombiner(VectorCombiner):
    def __init__(self, single_path, pairwise_path, balancer_fn=None):
        super().__init__(single_path, pairwise_path)
        if balancer_fn is None:
            balancer_fn = lambda x: True
        self._balancer = balancer_fn

    def stream(self, user_ids=None, max_lines=None):
        """Stream vectors, optionally filtering by user_ids and using a max.

        user_ids: If not None, a set of user_ids we are interested in. Only pairwise vectors
        where user_ids in the first position are in this set are considered.
        max_lines: If not None, the maximum amount of lines to stream.
        """
        single_vects_by_id = self._single_vects_by_id()

        lines = self.stream_csv(self.pairwise_path)
        if user_ids is not None:
            lines = self._filter_user_ids(lines, user_ids)
        lines = self._filter_balance(lines)
        if max_lines is not None:
            lines = self._filter_max(lines, max_lines)
        return self._map_combine(lines, single_vects_by_id)

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

    def _map_combine(self, lines, single_vects_by_id):
        for line in lines:
            yield self._combine_vects(line, single_vects_by_id)

    def _filter_max(self, lines, max_lines):
        counter = 0
        for line in lines:
            yield line
            counter += 1
            if counter >= max_lines:
                return


class BalancedFriendCombiner(BalancedVectorCombiner):
    def __init__(self, single_path, pairwise_path):
        super().__init__(single_path, pairwise_path)

        friend_index = self.pairwise_header.index('areFriends')
        def are_friends(line):
            return int(line[friend_index]) > 0
        self._balancer = are_friends


class BalancedPCCCombiner(BalancedVectorCombiner):
    def __init__(self, single_path, pairwise_path):
        super().__init__(single_path, pairwise_path)

        pcc_index = self.pairwise_header.index('PCC')
        def pos_pcc(line):
            pcc = line[pcc_index]
            if pcc == 'null':
                return None
            return float(pcc) > 0
        self._balancer = pos_pcc

