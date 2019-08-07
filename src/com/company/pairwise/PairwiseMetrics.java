package com.company.pairwise;

import com.company.data_interface.User;

import java.util.HashSet;

public class PairwiseMetrics {
    private static double AVG_REVIEW_SCORE = 3.7161;

    public static boolean areFriends(User user1, User user2) {
        return user2.getUserId().contains(user2.getUserId());
    }

    public static double reviewPcc(User user1, User user2, int minOverlap) {
        HashSet<String> mutuallyReviewed = new HashSet<>(user1.getItemsReviewed());
        mutuallyReviewed.retainAll(user2.getItemsReviewed());
        if (mutuallyReviewed.size() < minOverlap) {
            return 0;
        }
        return 0;
    }
}
