package agpar.multifacet.pairwise.review_avg_calculators;

import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.User;

public class UserReviewAvgCalculator extends ReviewAvgCalculator {

    public UserReviewAvgCalculator(ReviewsById reviewsByItems) {
        super(reviewsByItems);
    }

    @Override
    public double[] getAvgs(Review[] reviews, User user) {
        double avg = this.getUserAvg(user);
        return this.toArray(avg, reviews.length);
    }
}
