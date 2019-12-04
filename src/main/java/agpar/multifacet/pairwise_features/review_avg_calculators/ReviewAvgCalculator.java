package agpar.multifacet.pairwise_features.review_avg_calculators;

import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// Supports methods for calculating averages of reviews, with support for multithreaded
// caching of results.
public abstract class ReviewAvgCalculator {
    private ReviewsById reviewsByItems;
    private ConcurrentHashMap<Integer, Double> avgItemReviews = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Double> avgUserReviews = new ConcurrentHashMap<>();
    private Double GLOBAL_AVG_REVIEW_SCORE;

    public ReviewAvgCalculator(ReviewsById reviewsByItems) {
        this.reviewsByItems = reviewsByItems;
    }

    public abstract double[] getAvgs(Review[] reviews, User user);

    public double getUserAvg(User user) {
        Double cachedAvg = this.avgUserReviews.get(user.getUserIdInt());
        if(cachedAvg != null) {
            return cachedAvg;
        } else {
            double val = this.avg(user.getReviews());
            this.avgUserReviews.put(user.getUserIdInt(), val);
            return val;
        }
    }

    public double getGlobalAvg() {
        if (this.GLOBAL_AVG_REVIEW_SCORE == null) {
            calculateGlobalAverage();
        }
        return this.GLOBAL_AVG_REVIEW_SCORE;
    }

    private synchronized void calculateGlobalAverage() {
        if (this.GLOBAL_AVG_REVIEW_SCORE == null) {
            int sum = 0;
            int count = 0;
            for (List<Review> reviewList : reviewsByItems.values()) {
                for (Review review : reviewList) {
                    sum += review.getStars();
                    count += 1;
                }
            }
            this.GLOBAL_AVG_REVIEW_SCORE = (((double) sum) / count);
        }
    }

    public double getItemAvg(int itemId) {
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

        if (count == 0) {
            throw new IllegalArgumentException("Can not compute average of 0 items");
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

