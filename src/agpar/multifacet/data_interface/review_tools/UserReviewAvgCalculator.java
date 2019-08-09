package agpar.multifacet.data_interface.review_tools;

import agpar.multifacet.data_interface.Review;
import agpar.multifacet.data_interface.ReviewsById;
import agpar.multifacet.data_interface.User;

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
