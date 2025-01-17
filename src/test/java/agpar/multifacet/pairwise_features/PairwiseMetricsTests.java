package agpar.multifacet.pairwise_features;

import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.epinions.EpinionsUser;
import agpar.multifacet.data_interface.yelp.YelpUser;
import agpar.multifacet.pairwise_features.review_avg_calculators.UserReviewAvgCalculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PairwiseMetricsTests {

    private double delta = 0.000001;

    @Before
    public void setup() {
        TrustGraph.resetGlobals();
    }

    @Test
    public void are_friends_mutual_is_true() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        user1.addTrustLink(user2.getUserId());
        user2.addTrustLink(user1.getUserId());

        assert(PairwiseMetrics.areFriends(user1, user2));
    }

    @Test
    public void are_friends_outgoing_is_true() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        user1.addTrustLink(user2.getUserId());

        assert(PairwiseMetrics.areFriends(user1, user2));
    }

    @Test
    public void yelp_are_friends_incoming_is_true() {
        User user1 = Mockito.spy(new YelpUser(1, "id"));
        User user2 = new YelpUser(2, "id");
        user2.addTrustLink(user1.getUserId());

        assert(PairwiseMetrics.areFriends(user1, user2));
    }

    @Test
    public void epinions_are_friends_incoming_is_false() {
        User user1 = new EpinionsUser(1);
        User user2 = new EpinionsUser(2);
        user2.addTrustLink(user1.getUserId());

        assert(!PairwiseMetrics.areFriends(user1, user2));
    }

    @Test
    public void jaccard_no_overlap_is_0() {
        Set<Integer> s1 = new HashSet<>(Arrays.asList(1, 2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(3, 4));
        Assert.assertEquals(0., PairwiseMetrics.jaccard(s1, s2), 0);
    }

    @Test
    public void jaccard_test_1_overlap() {
        Set<Integer> s1 = new HashSet<>(Arrays.asList(1, 2));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(2, 3));
        Assert.assertEquals(1./3., PairwiseMetrics.jaccard(s1, s2), delta);
    }

    @Test
    public void jaccard_test_all_overlap() {
        Set<Integer> s1 = new HashSet<>(Arrays.asList(1, 2));
        Assert.assertEquals(1., PairwiseMetrics.jaccard(s1, s1), delta);
    }

    @Test
    public void review_pcc_perfect_correlation() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");

        user1.addReviews(Arrays.asList(
                new Review(1, 1, "", 0),
                new Review(1, 2, "", 0),
                new Review(1, 3, "", 5)
        ));
        user2.addReviews(Arrays.asList(
                new Review(2, 1, "", 0),
                new Review(2, 2, "", 0),
                new Review(2, 3, "", 5)
        ));

        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, new UserReviewAvgCalculator(null), 3);
        Assert.assertNotNull(pcc);
        Assert.assertEquals(1., pcc, delta);
    }

    @Test
    public void review_pcc_perfect_anti_correlation() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");

        user1.addReviews(Arrays.asList(
                new Review(1, 1, "", 0),
                new Review(1, 2, "", 0),
                new Review(1, 3, "", 5)
        ));
        user2.addReviews(Arrays.asList(
                new Review(2, 1, "", 5),
                new Review(2, 2, "", 5),
                new Review(2, 3, "", 0)
        ));

        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, new UserReviewAvgCalculator(null), 3);
        Assert.assertNotNull(pcc);
        Assert.assertEquals(-1, pcc,  delta);
    }

    @Test
    public void review_pcc_users_with_insufficient_overlapping_returns_null() {

        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");

        user1.addReviews(Arrays.asList(
                new Review(1, 1, "", 0),
                new Review(1, 2, "", 5)
        ));
        user2.addReviews(Arrays.asList(
                new Review(2, 1, "", 5),
                new Review(2, 2, "", 0)
        ));
        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, new UserReviewAvgCalculator(null), 3);
        Assert.assertNull(pcc);
    }
}
