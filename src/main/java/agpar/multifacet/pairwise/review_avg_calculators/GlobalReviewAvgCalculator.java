package agpar.multifacet.pairwise.review_avg_calculators;

import agpar.multifacet.data_interface.yelp.data_classes.Review;
import agpar.multifacet.data_interface.yelp.collections.ReviewsById;
import agpar.multifacet.data_interface.yelp.data_classes.User;

public class GlobalReviewAvgCalculator extends ReviewAvgCalculator{
    public GlobalReviewAvgCalculator(ReviewsById reviewsByItems) {
        super(reviewsByItems);
    }

    @Override
    public double[] getAvgs(Review[] reviews, User user) {
        return this.toArray(this.getGlobalAvg(), reviews.length);
    }
}
