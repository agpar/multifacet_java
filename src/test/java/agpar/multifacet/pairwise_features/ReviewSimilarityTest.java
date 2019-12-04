package agpar.multifacet.pairwise_features;

import agpar.multifacet.data_interface.data_classes.Review;
import org.junit.Assert;
import org.junit.Test;

public class ReviewSimilarityTest {

    @Test
    public void pcc_full_correlation() {
        Review[] reviews1 = new Review[] {
                new Review("test", 1, 1, "", 5),
                new Review("test", 1, 2, "", 0),
        };
        Review[] reviews2 = new Review[] {
                new Review("test", 2, 1, "", 5),
                new Review("test", 2, 2, "", 0),
        };
        double[] reviewAvgs = new double[] {2.5, 2.5};
        double pcc = ReviewSimilarity.pcc(reviews1, reviewAvgs, reviews2, reviewAvgs);
        Assert.assertEquals(1.0, pcc, 0.00001);
    }

    @Test
    public void pcc_full_anti_correlation() {
        Review[] reviews1 = new Review[] {
                new Review("test", 1, 1, "", 0),
                new Review("test", 1, 2, "", 5),
        };
        Review[] reviews2 = new Review[] {
                new Review("test", 2, 1, "", 5),
                new Review("test", 2, 2, "", 0),
        };
        double[] reviewAvgs = new double[] {2.5, 2.5};
        double pcc = ReviewSimilarity.pcc(reviews1, reviewAvgs, reviews2, reviewAvgs);
        Assert.assertEquals(-1.0, pcc, 0.0);
    }

    @Test
    public void pcc_fails_when_reviews_are_out_of_order() {
        Review[] reviews1 = new Review[] {
                new Review("test", 1, 2, "", 0),
                new Review("test", 1, 1, "", 5),
        };
        Review[] reviews2 = new Review[] {
                new Review("test", 2, 1, "", 5),
                new Review("test", 2, 2, "", 0),
        };

        double[] reviewAvgs = new double[] {2.5, 2.5};
        try {
            ReviewSimilarity.pcc(reviews1, reviewAvgs, reviews2, reviewAvgs);
        } catch (AssertionError e) {
            return;
        }
        Assert.fail("Method did not throw assertion error.");
    }

    @Test
    public void pcc_fails_when_review_lens_differ() {
        Review[] reviews1 = new Review[] {
                new Review("test", 1, 2, "", 0),
                new Review("test", 1, 1, "", 5),
        };
        Review[] reviews2 = new Review[] {
                new Review("test", 2, 1, "", 5),
        };
        double[] reviewAvgs = new double[] {2.5, 2.5};
        try {
            ReviewSimilarity.pcc(reviews1, reviewAvgs, reviews2, reviewAvgs);
        } catch (AssertionError e) {
            return;
        }
        Assert.fail("Method did not throw assertion error.");
    }
}