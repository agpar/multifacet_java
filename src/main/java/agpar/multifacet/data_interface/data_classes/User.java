package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.collections.ReviewList;
import com.google.gson.JsonObject;

import java.util.*;

public class User {
    private String userId;
    private int userIdInt;
    private HashSet<Integer> friendsInt;
    private HashSet<Integer> categoriesReviewed = new HashSet<>();
    private ReviewList reviews;

    public User(String userId, int userIdInt, HashSet<Integer> friendsInt) {
        this.userId = userId;
        this.userIdInt = userIdInt;
        this.friendsInt = friendsInt;
    }

    public static User fromJson(JsonObject obj) {
        String userId = obj.get("true_user_id").getAsString();
        int userIdInt = obj.get("user_id").getAsInt();
        String friendStr = obj.get("friends").getAsString();
        Collection<String> friendSplit = Arrays.asList(friendStr.split(", "));
        HashSet<Integer> friendsInt = new HashSet<>();
        for(String friendId : friendSplit) {
            friendsInt.add(Integer.valueOf(friendId));
        }
        return new User(userId, userIdInt, friendsInt);
    }

    public String getUserId() {
        return this.userId;
    }

    public int getUserIdInt() {
        return this.userIdInt;
    }

    public Set<Integer> getFriendsLinksOutgoing() {
        return Collections.unmodifiableSet(this.friendsInt);
    }

    public Set<Integer> getFriendsLinksIncoming() { return Collections.unmodifiableSet(this.friendsInt); }

    public void addReviews(List<Review> reviews) {
       this.reviews = new ReviewList(reviews);
    }

    public ReviewList getReviews() {
        return this.reviews;
    }

    public Set<Integer> getItemsReviewed() {
        return this.reviews.getItemsReviewed();
    }

    public Set<Integer> getCategoriesReviewed() {
        return this.categoriesReviewed;
    }
}
