from unittest import TestCase
from vector_combiners.text_vector_combiner import TextVectorCombiner
from vector_combiners.binary_vector_combiner import BinaryVectorCombiner
import numpy as np


class TestTextCombiners(TestCase):
    SINGLE_CSV = "./vector_combiners/tests/test_data/single_feats.csv"
    PAIR_CSV = "./vector_combiners/tests/test_data/pairwise_feats.csv"
    SINGLE_NPZ = "./vector_combiners/tests/test_data/single_feats.npz"
    PAIR_NPZ = "./vector_combiners/tests/test_data/pairwise_feats.npz"

    FIRST_VECT = np.array(
        [0.23076923076923078, 1.0, 0.010520869265263884, 0.03857652063930091, 0.04519309778142974, 0.1561945331913383,
         7.306159211988808e-05, 0.06640446224256293, 0.14752886500061252, 0.2551042188099338, 0.4108241572250652,
         0.8461538461538461, 1.0, 0.0015810049442336438, 0.0015810049442336438, 0.010271158586688579,
         0.009681479330041631, 3.856109620082058e-05, 0.0014874141876430205, 0.011606206470002958, 0.6819114492050549,
         0.3497433504433038, float('nan'), 0, 1, 0.403670, 0, float('nan'), 0.003563])

    def assert_vects_equal(self, a, b):
        self.assertEqual(len(a), len(b))
        for a1, b1 in zip(a, b):
            if np.isnan(a1):
                self.assertTrue(np.isnan(b1))
            else:
                self.assertAlmostEqual(a1, b1)

    def test_text_steam_one(self):
        tvc = TextVectorCombiner(self.SINGLE_CSV, self.PAIR_CSV)
        ids, data = next(tvc.stream())
        self.assertTrue(np.array_equal(ids, np.array([166, 343])))
        self.assert_vects_equal(self.FIRST_VECT, data)

    def test_binary_steam_one(self):
        bvc = BinaryVectorCombiner(self.SINGLE_NPZ, self.PAIR_NPZ)
        ids, data = next(bvc.stream())
        self.assertTrue(np.array_equal(ids, np.array([166, 343])))
        self.assert_vects_equal(self.FIRST_VECT, data)

    def test_text_binary_equivalence(self):
        tvc = TextVectorCombiner(self.SINGLE_CSV, self.PAIR_CSV)
        bvc = BinaryVectorCombiner(self.SINGLE_NPZ, self.PAIR_NPZ)
        tvc_list = list(tvc.stream())
        bvc_list = list(bvc.stream())
        self.assertEqual(len(tvc_list), len(bvc_list))
        for t1, b1 in zip(tvc_list, bvc_list):
            t1_ids, t1_data = t1
            b1_ids, b1_data = b1
            self.assert_vects_equal(t1_ids, b1_ids)
            self.assert_vects_equal(t1_data, b1_data)

