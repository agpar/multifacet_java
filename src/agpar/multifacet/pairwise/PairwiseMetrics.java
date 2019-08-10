package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.data_interface.collections.ReviewList;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.HashSet;
import java.util.Set;

public class PairwiseMetrics {
    public static boolean areFriends(User user1, User user2) {
        return user1.getFriends().contains(user2.getUserId());
    }

    public static boolean areFriendsOfFriends(User user1, User user2, UsersById usersById) {
        if (PairwiseMetrics.areFriends(user1, user2)) {
            return true;
        }
        User lessFriends = user1.getFriends().size() < user2.getFriends().size() ? user1 : user2;
        User moreFriends = user1 == lessFriends ? user2 : user1;
        for (String friendId : lessFriends.getFriends()) {
            User bridgeFriend = usersById.get(friendId);
            if (bridgeFriend.getFriends().contains(moreFriends.getUserId())) return true;
        }
        return false;
    }

    public static Double reviewPcc(User user1, User user2, ReviewAvgCalculator avgCalculator, int minOverlap) {
        HashSet<String> mutuallyReviewed = new HashSet<>(user1.getItemsReviewed());
        mutuallyReviewed.retainAll(user2.getItemsReviewed());
        if (mutuallyReviewed.size() < minOverlap) {
            return null;
        }

        Review[] reviews1 = PairwiseMetrics.filterReviews(user1.getReviews(), mutuallyReviewed);
        Review[] reviews2 = PairwiseMetrics.filterReviews(user2.getReviews(), mutuallyReviewed);
        double[] avgs1 = avgCalculator.getAvgs(reviews1, user1);
        double[] avgs2 = avgCalculator.getAvgs(reviews2, user2);

        return ReviewSimilarity.pcc(reviews1, avgs1, reviews2, avgs2);
    }

    public static double socialJaccard(User user1, User user2) {
        Set<String> union = new HashSet<String>(user1.getFriends());
        union.retainAll(user2.getFriends());
        int numer = union.size();
        if (numer == 0) return 0;
        return numer / (user1.getFriends().size() + user2.getFriends().size());
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
