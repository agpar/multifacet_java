package com.company.data_interface.review_tools;

import com.company.data_interface.Review;
import com.company.data_interface.ReviewsById;
import com.company.data_interface.User;

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
