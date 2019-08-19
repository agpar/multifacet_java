package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.data_interface.collections.ReviewList;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.HashSet;
import java.util.Set;

public class PairwiseMetrics {
    public static boolean areFriends(User user1, User user2) {
        return user1.getFriendsInt().contains(user2.getUserIdInt());
    }

    public static Double reviewPcc(User user1, User user2, ReviewAvgCalculator avgCalculator, int minOverlap) {
        HashSet<Integer> mutuallyReviewed = new HashSet<>(user1.getItemsReviewed());
        mutuallyReviewed.retainAll(user2.getItemsReviewed());
        if (mutuallyReviewed.size() < minOverlap) {
            //TODO should this be null or 0?
            return null;
        }

        Review[] reviews1 = PairwiseMetrics.filterReviews(user1.getReviews(), mutuallyReviewed);
        Review[] reviews2 = PairwiseMetrics.filterReviews(user2.getReviews(), mutuallyReviewed);
        double[] avgs1 = avgCalculator.getAvgs(reviews1, user1);
        double[] avgs2 = avgCalculator.getAvgs(reviews2, user2);

        return ReviewSimilarity.pcc(reviews1, avgs1, reviews2, avgs2);
    }

    protected static <T> double jaccard(Set<T> set1, Set<T> set2) {
        Set<T> intersection = new HashSet<T>(set1);
        intersection.retainAll(set2);
        if (intersection.size() == 0) {
            return 0.0;
        }
        Set<T> union = new HashSet<T>(set1);
        union.addAll(set2);
        return ((double) intersection.size()) / union.size();
    }

    public static double socialJaccard(User user1, User user2) {
        return PairwiseMetrics.jaccard(user1.getFriendsInt(), user2.getFriendsInt());
    }

    public static double itemJaccard(User user1, User user2) {
        return PairwiseMetrics.jaccard(user1.getItemsReviewed(), user2.getItemsReviewed());
    }

    public static double categoryJaccard(User user1, User user2) {
        return PairwiseMetrics.jaccard(user1.getCategoriesReviewed(), user2.getCategoriesReviewed());
    }

    public static Review[] filterReviews(ReviewList reviews, HashSet<Integer> itemSet) {
        Review[] filteredReviews = new Review[itemSet.size()];
        int i = 0;
        for (Review review : reviews) {
            if (itemSet.contains(review.getItemIdInt())) {
                filteredReviews[i] = review;
                i++;
            }
        }
        return filteredReviews;
    }
}
