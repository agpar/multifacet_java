package agpar.multifacet.data_interface.review_tools;

import agpar.multifacet.data_interface.Review;
import agpar.multifacet.data_interface.ReviewsById;
import agpar.multifacet.data_interface.User;

public class GlobalReviewAvgCalculator extends ReviewAvgCalculator{
    public GlobalReviewAvgCalculator(ReviewsById reviewsByItems) {
        super(reviewsByItems);
    }

    @Override
    public double[] getAvgs(Review[] reviews, User user) {
        return this.toArray(this.getGlobalAvg(), reviews.length);
    }
}
