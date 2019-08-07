package com.company.data_interface;

import com.google.gson.JsonObject;

public class Review {
    private String reviewId;
    private String userId;
    private String itemId;
    private String date;
    private double stars;


    public Review(String reviewId, String userId, String itemId, String date, double stars) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.itemId = itemId;
        this.date = date;
        this.stars = stars;
    }

    public static Review fromJson(JsonObject obj) {
        return new Review(
                obj.get("review_id").getAsString(),
                obj.get("user_id").getAsString(),
                obj.get("business_id").getAsString(),
                obj.get("date").getAsString(),
                obj.get("stars").getAsDouble()
        );
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDate() {
        return date;
    }

    public double getStars() {
        return stars;
    }
}
