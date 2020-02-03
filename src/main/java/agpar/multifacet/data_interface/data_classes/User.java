package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.collections.ReviewList;
import com.google.gson.JsonObject;

import java.util.*;

public class User {
    private String trueUserId;
    private int userId;
    private HashSet<Integer> friends;
    private HashSet<Integer> categoriesReviewed = new HashSet<>();
    private HashSet<Region> regionsReviewed = new HashSet<>();
    private ReviewList reviews = new ReviewList(new ArrayList<>());

    public User(String trueUserId, int userIdInt, HashSet<Integer> friends) {
        this.trueUserId = trueUserId;
        this.userId = userIdInt;
        this.friends = friends;
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

    public String getTrueUserId() {
        return this.trueUserId;
    }

    public int getUserId() {
        return this.userId;
    }

    public Set<Integer> getFriendsLinksOutgoing() {
        return Collections.unmodifiableSet(this.friends);
    }

    public Set<Integer> getFriendsLinksIncoming() { return Collections.unmodifiableSet(this.friends); }

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

    public Set<Region> getRegionsReviewed() {
        return this.regionsReviewed;
    }
}
