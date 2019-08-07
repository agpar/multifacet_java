package com.company;

import com.company.data_interface.*;

import java.util.List;

public class YelpData {
    private UsersById users;
    private ReviewsByItem reviews;
    private DataReader reader;

    public YelpData(DataReader reader) {
        this.reader = reader;
    }

    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.users = reader.loadUsers(start, stop);
        System.out.println("Loading Reviews");
        this.reviews = reader.loadReviews(0, 10000000);

        // Adding reviews to users.
        for (List<Review> reviews : this.reviews.values()) {
            for (Review review : reviews) {
                if (this.users.containsKey(review.getUserId())) {
                    this.users.get(review.getUserId()).getReviews().add(review);
                }
            }
        }

        // TODO need to remove dupes.

        // Compute review sets.
        for (User user : this.users.values()) {
            user.initItemsReviewed();
            user.removeReviewDupes();
        }

    }
}
