package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.io.IdStringToIntMap;
import com.google.gson.JsonObject;

public class Review {
    private String reviewId;
    private String userId;
    private String itemId;
    private int itemIdInt;
    private String date;
    private double stars;


    public Review(String reviewId, String userId, String itemId, int itemIdInt, String date, double stars) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.itemId = itemId;
        this.itemIdInt = itemIdInt;
        this.date = date;
        this.stars = stars;
    }

    public static Review fromJson(JsonObject obj, IdStringToIntMap itemIdMap) {
        String itemId = obj.get("business_id").getAsString();
        return new Review(
                obj.get("review_id").getAsString(),
                obj.get("user_id").getAsString(),
                itemId,
                itemIdMap.getInt(itemId),
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
