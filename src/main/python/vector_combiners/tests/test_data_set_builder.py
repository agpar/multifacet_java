from unittest import TestCase

from vector_combiners import BalancedDataSetBuilder, TextVectorCombiner, BalancedFriendDataSetBuilder, \
    BalancedPCCDataSetBuilder
from vector_combiners.binary_vector_combiner import BinaryVectorCombiner


class TestDataSetBuilder(TestCase):
    SINGLE_CSV = "./vector_combiners/tests/test_data/single_feats.csv"
    PAIR_CSV = "./vector_combiners/tests/test_data/pairwise_feats.csv"
    SINGLE_NPZ = "./vector_combiners/tests/test_data/single_feats.npz"
    PAIR_NPZ = "./vector_combiners/tests/test_data/pairwise_feats.npz"

    def test_binary_integration(self):
        bvc = BinaryVectorCombiner(self.SINGLE_NPZ, self.PAIR_NPZ)
        bds = BalancedDataSetBuilder(bvc)

        pair_ids, pair_data = next(bds.stream(user_ids={0}))
        self.assertEqual(pair_ids[0], 0)

    def test_text_integration(self):
        tvc = TextVectorCombiner(self.SINGLE_CSV, self.PAIR_CSV)
        bds = BalancedDataSetBuilder(tvc)

        pair_ids, pair_data = next(bds.stream(user_ids={0}))
        self.assertEqual(pair_ids[0], 0)

    def test_friend_balancer(self):
        tvc = TextVectorCombiner(self.SINGLE_CSV, self.PAIR_CSV)
        bfd = BalancedFriendDataSetBuilder(tvc)
        balanced_friends = list(bfd.stream())

        friend_index = tvc.header.index('areFriends') - 2
        pos_examples = len([1 for i, d in balanced_friends if d[friend_index]])
        neg_examples = len([1 for i, d in balanced_friends if not d[friend_index]])
        self.assertTrue(pos_examples - neg_examples >= 0)

    def test_pcc_balancer(self):
        tvc = TextVectorCombiner(self.SINGLE_CSV, self.PAIR_CSV)
        bpd = BalancedPCCDataSetBuilder(tvc)
        balanced_pcc = list(bpd.stream())

        pcc_index = tvc.header.index('PCC') - 2
        pos_examples = len([1 for i, d in balanced_pcc if d[pcc_index] > 0])
        neg_examples = len([1 for i, d in balanced_pcc if d[pcc_index] < 0])
        self.assertTrue(pos_examples - neg_examples >= 0)