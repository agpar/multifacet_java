package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.io.IdStringToIntMap;
import com.google.gson.JsonObject;

public class Review {
    private String reviewId;
    private int userIdInt;
    private int itemIdInt;
    private String date;
    private double stars;


    public Review(String reviewId, int userIdInt, int itemIdInt, String date, double stars) {
        this.reviewId = reviewId;
        this.userIdInt = userIdInt;
        this.itemIdInt = itemIdInt;
        this.date = date;
        this.stars = stars;
    }

    public static Review fromJson(JsonObject obj) {
        return new Review(
                obj.get("true_review_id").getAsString(),
                obj.get("user_id").getAsInt(),
                obj.get("business_id").getAsInt(),
                obj.get("date").getAsString(),
                obj.get("stars").getAsDouble()
        );
    }

    public String getReviewId() {
        return reviewId;
    }

    public int getUserIdInt() {
        return userIdInt;
    }

    public int getItemIdInt() {
        return itemIdInt;
    }

    public String getDate() {
        return date;
    }

    public double getStars() {
        return stars;
    }
}
