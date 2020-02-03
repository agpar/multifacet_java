package agpar.multifacet.pairwise_features.review_avg_calculators;

import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.User;

public class ItemReviewAvgCalculator extends ReviewAvgCalculator {
    public ItemReviewAvgCalculator(ReviewsById reviewsByItems) {
        super(reviewsByItems);
    }

    @Override
    public double[] getAvgs(Review[] reviews, User user) {
        double[] output = new double[reviews.length];
        for(int i = 0; i < reviews.length; i++) {
            output[i] = this.getItemAvg(reviews[i].getItemId());
        }
        return output;
    }
}
