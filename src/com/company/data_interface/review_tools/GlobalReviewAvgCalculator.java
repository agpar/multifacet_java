package com.company.data_interface.review_tools;

import com.company.data_interface.Review;
import com.company.data_interface.ReviewsById;
import com.company.data_interface.User;

public class GlobalReviewAvgCalculator extends ReviewAvgCalculator{
    public GlobalReviewAvgCalculator(ReviewsById reviewsByItems) {
        super(reviewsByItems);
    }

    @Override
    public double[] getAvgs(Review[] reviews, User user) {
        return this.toArray(this.getGlobalAvg(), reviews.length);
    }
}
