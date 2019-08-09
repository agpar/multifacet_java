package com.company.data_interface.review_tools;

import com.company.data_interface.Review;
import com.company.data_interface.ReviewsById;
import com.company.data_interface.User;

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
