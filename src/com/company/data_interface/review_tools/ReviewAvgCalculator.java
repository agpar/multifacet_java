package com.company.data_interface.review_tools;

import com.company.data_interface.Review;
import com.company.data_interface.ReviewsById;
import com.company.data_interface.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Supports methods for calculating averages of reviews, with support for multithreaded
// caching of results.
public abstract class ReviewAvgCalculator {
    private ReviewsById reviewsByItems;
    private ConcurrentHashMap<String, Double> avgItemReviews = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Double> avgUserReviews = new ConcurrentHashMap<>();
    private static double GLOBAL_AVG_REVIEW_SCORE = 3.7161;

    public ReviewAvgCalculator(ReviewsById reviewsByItems) {
        this.reviewsByItems = reviewsByItems;
    }

    public abstract double[] getAvgs(Review[] reviews, User user);

    public double getUserAvg(User user) {
        Double cachedAvg = this.avgUserReviews.get(user.getUserId());
        if(cachedAvg != null) {
            return cachedAvg;
        } else {
            double val = this.avg(user.getReviews());
            this.avgUserReviews.put(user.getUserId(), val);
            return val;
        }
    }

    public double getGlobalAvg() {
        return this.GLOBAL_AVG_REVIEW_SCORE;
    }

    public double getItemAvg(String itemId) {
        Double cachedAvg = this.avgItemReviews.get(itemId);
        if(cachedAvg != null) {
            return cachedAvg;
        } else {
            List<Review> reviewsForItem = this.reviewsByItems.get(itemId);
            double val = this.avg(reviewsForItem);
            this.avgItemReviews.put(itemId, val);
            return val;
        }
    }

    private double avg(Iterable<Review> reviews) {
        double sum = 0;
        int count = 0;
        for (Review review : reviews) {
            sum += review.getStars();
            count++;
        }
        return sum / count;
    }

    protected double[] toArray(double avg, int size) {
        double[] output = new double[size];
        for(int i = 0; i < size; i++) {
            output[i] = avg;
        }
        return output;
    }
}

