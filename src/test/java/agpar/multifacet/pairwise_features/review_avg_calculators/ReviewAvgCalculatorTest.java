package agpar.multifacet.pairwise_features.review_avg_calculators;

import agpar.multifacet.MockedDataSet;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.yelp.YelpUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class ReviewAvgCalculatorTest {

    private MockedDataSet data;
    private User user1;

    @Before
    public void setup(){
        data = new MockedDataSet()    ;
        user1 = new YelpUser(1, "id");
        ArrayList<Review> reviews1 = new ArrayList<>(List.of(
                new Review(1, 1, "", 0),
                new Review(1, 2, "", 3),
                new Review(1, 3, "", 5)
        ));
        user1.addReviews(reviews1);
        data.registerReviews(reviews1);

        User user2 = new YelpUser(2, "id");
        ArrayList<Review> reviews2 = new ArrayList<>(List.of(
                new Review(2, 1, "", 5)
        ));
        user2.addReviews(reviews2);
        data.registerReviews(reviews2);;
        data.registerUsers(List.of(user1, user2));
    }

    @Test
    public void global_avg_is_correct() {
        double globalAverage = new GlobalReviewAvgCalculator(data.dataset.getReviewsByItemId()).getGlobalAvg();
        Assert.assertEquals(13. / 4, globalAverage, 0.0001);
    }

    @Test
    public void user_avg_is_correct() {
        double userAverage = new UserReviewAvgCalculator(data.dataset.getReviewsByItemId()).getUserAvg(user1);
        Assert.assertEquals(8. / 3, userAverage, 0.0001);
    }

    @Test
    public void user_avg_fails_with_no_reviews() {
        User user = new YelpUser(1, "id");
        try {
            double userAverage = new UserReviewAvgCalculator(data.dataset.getReviewsByItemId()).getUserAvg(user);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("Avg of 0 reviews proceeded silently.");
    }

    @Test
    public void item_avg_is_correct() {
        double itemAvg = new ItemReviewAvgCalculator(data.dataset.getReviewsByItemId()).getItemAvg(1);
        Assert.assertEquals(5. / 2, itemAvg, 0.0001);
    }

}