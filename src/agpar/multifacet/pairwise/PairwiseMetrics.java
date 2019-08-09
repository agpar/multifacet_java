package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.Review;
import agpar.multifacet.data_interface.review_tools.ReviewAvgCalculator;
import agpar.multifacet.data_interface.review_tools.ReviewList;
import agpar.multifacet.data_interface.User;

import java.util.HashSet;

public class PairwiseMetrics {
    public static boolean areFriends(User user1, User user2) {
        return user2.getUserId().contains(user2.getUserId());
    }

    public static double reviewPcc(User user1, User user2, ReviewAvgCalculator avgCalculator, int minOverlap) {
        HashSet<String> mutuallyReviewed = new HashSet<>(user1.getItemsReviewed());
        mutuallyReviewed.retainAll(user2.getItemsReviewed());
        if (mutuallyReviewed.size() < minOverlap) {
            return 0;
        }

        Review[] reviews1 = PairwiseMetrics.filterReviews(user1.getReviews(), mutuallyReviewed);
        Review[] reviews2 = PairwiseMetrics.filterReviews(user2.getReviews(), mutuallyReviewed);
        double[] avgs1 = avgCalculator.getAvgs(reviews1, user1);
        double[] avgs2 = avgCalculator.getAvgs(reviews2, user2);

        return ReviewSimilarity.pcc(reviews1, avgs1, reviews2, avgs2);
    }

    public static Review[] filterReviews(ReviewList reviews, HashSet<String> itemSet) {
        Review[] filteredReviews = new Review[itemSet.size()];
        int i = 0;
        for (Review review : reviews) {
            if (itemSet.contains(review.getItemId())) {
                filteredReviews[i] = review;
                i++;
            }
        }
        return filteredReviews;
    }
}
