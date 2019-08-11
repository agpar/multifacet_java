package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.io.IdStringToIntMap;
import agpar.multifacet.data_interface.collections.ReviewList;
import com.google.gson.JsonObject;

import java.util.*;

public class User {
    private String userId;
    private int userIdInt;
    private HashSet<String> friends;
    private HashSet<Integer> friendsInt;
    private ReviewList reviews;

    public User(String userId, int userIdInt, HashSet<String> friends, HashSet<Integer> friendsInt) {
        this.userId = userId;
        this.userIdInt = userIdInt;
        this.friends = friends;
        this.friendsInt = friendsInt;
    }

    public static User fromJson(JsonObject obj, IdStringToIntMap userIdMap) {
        String userId = obj.get("user_id").getAsString();
        int userIdInt = userIdMap.getInt(userId);
        String friendStr = obj.get("friends").getAsString();
        Collection<String> friendSplit = Arrays.asList(friendStr.split(", "));
        HashSet<String> friends = new HashSet<String>(friendSplit);
        HashSet<Integer> friendsInt = new HashSet<>();
        for(String friendId : friendSplit) {
            friendsInt.add(userIdMap.getInt(friendId));
        }
        return new User(userId, userIdInt, friends, friendsInt);
    }

    public String getUserId() {
        return this.userId;
    }

    public int getUserIdInt() {
        return this.userIdInt;
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(this.friends);
    }

    public Set<Integer> getFriendsInt() {
        return Collections.unmodifiableSet(this.friendsInt);
    }

    public void addReviews(List<Review> reviews) {
       this.reviews = new ReviewList(reviews);
    }

    public ReviewList getReviews() {
        return this.reviews;
    }

    public Set<Integer> getItemsReviewed() {
        return this.reviews.getItemsReviewed();
    }
}
