package agpar.multifacet.data_interface;

import agpar.multifacet.data_interface.review_tools.ReviewList;
import com.google.gson.JsonObject;

import java.util.*;

public class User {
    private String userId;
    private HashSet<String> friends;
    private ReviewList reviews;

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

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(this.friends);
    }

    public void addReviews(List<Review> reviews) {
       this.reviews = new ReviewList(reviews);
    }

    public ReviewList getReviews() {
        return this.reviews;
    }

    public Set<String> getItemsReviewed() {
        return this.reviews.getItemsReviewed();
    }
}
