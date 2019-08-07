package com.company.data_interface;

import com.google.gson.JsonObject;

import java.util.*;

public class User {
    private String userId;
    private HashSet<String> friends;
    private HashSet<String> itemsReviewed;
    private ArrayList<Review> reviews = new ArrayList<>();

    public User(String userId, HashSet<String> friends) {
        this.userId = userId;
        this.friends = friends;
    }

    public static User fromJson(JsonObject obj) {
        String userId = obj.get("user_id").getAsString();
        String friendStr = obj.get("friends").getAsString();
        Collection<String> friendSplit = Arrays.asList(friendStr.split(", "));
        HashSet<String> friends = new HashSet<String>(friendSplit);
        return new User(userId, friends);
    }

    public String getUserId() {
        return this.userId;
    }

    public HashSet<String> getFriends() {
        return this.friends;
    }

    public ArrayList<Review> getReviews() {
        return this.reviews;
    }

    public HashSet<String> getItemsReviewed() {
        return this.itemsReviewed;
    }
    
    public void initItemsReviewed() {
        this.itemsReviewed = new HashSet<>();
        for (Review review : this.reviews) {
            this.itemsReviewed.add(review.getItemId());
        }
    }

    public void removeReviewDupes() {
        if (this.itemsReviewed == null) {
            this.initItemsReviewed();
        }
        this.reviews.sort((review1, review2) -> review1.getItemId().compareTo(review2.getItemId()));
        if (this.itemsReviewed.size() == this.reviews.size()) return;

        int i = 0;
        ArrayList<Review> dedupedReviews = new ArrayList<>();
        String currentItem = this.reviews.get(0).getItemId();
        Review latestReviewForItem = this.reviews.get(0);
        while (i < this.reviews.size() - 1) {
            Review nextReview = this.reviews.get(i + 1);
            if (nextReview.getItemId().equals(currentItem)) {
                if(nextReview.getDate().compareTo(latestReviewForItem.getDate()) > 0) {
                    latestReviewForItem = nextReview;
                }
            } else {
                dedupedReviews.add(latestReviewForItem);
                currentItem = nextReview.getItemId();
                latestReviewForItem = nextReview;
            }
            i += 1;
        }
        dedupedReviews.add(latestReviewForItem);
        this.reviews = dedupedReviews;
    }
}
