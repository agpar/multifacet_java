package com.company;

import com.company.data_interface.*;

import java.util.Collection;
import java.util.List;

public class YelpData {
    private UsersById usersById;
    private ReviewsById reviewsByItemId;
    private DataReader reader;

    public YelpData(DataReader reader) {
        this.reader = reader;
    }

    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.usersById = reader.loadUsers(start, stop);
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadReviews(0, 10000000);

        // Adding reviews to users.
        ReviewsById reviewsByUserId = new ReviewsById();
        for (List<Review> reviews: this.reviewsByItemId.values()) {
            for (Review review: reviews) {
                reviewsByUserId.put(review.getUserId(), review);
            }
        }

        for (User user : this.usersById.values()) {
            user.addReviews(reviewsByUserId.get(user.getUserId()));
        }
        System.out.println("Done");
    }

    public ReviewsById getReviewsByItemId() {
        return this.reviewsByItemId;
    }

    public Collection<User> getUsers() {
        return this.usersById.values();
    }
}
