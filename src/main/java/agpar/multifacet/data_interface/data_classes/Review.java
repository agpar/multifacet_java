package agpar.multifacet.data_interface.data_classes;

import com.google.gson.JsonObject;

public class Review {
    private int userId;
    private int itemId;
    private String date;
    private double stars;


    public Review(int userId, int itemId, String date, double stars) {
        this.userId = userId;
        this.itemId = itemId;
        this.date = date;
        this.stars = stars;
    }

    public static Review fromJson(JsonObject obj) {
        return new Review(
                obj.get("user_id").getAsInt(),
                obj.get("business_id").getAsInt(),
                obj.get("date").getAsString(),
                obj.get("stars").getAsDouble()
        );
    }

    public int getUserId() {
        return userId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getDate() {
        return date;
    }

    public double getStars() {
        return stars;
    }
}
