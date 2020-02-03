package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.collections.ReviewList;
import com.google.gson.JsonObject;

import java.util.*;

public abstract class User {
    private String trueUserId;
    private int userId;
    private ReviewList reviews = new ReviewList(new ArrayList<>());
    private HashSet<Integer> categoriesReviewed = new HashSet<Integer>();

    public User(int userIdInt, String trueUserId) {
        this.trueUserId = trueUserId;
        this.userId = userIdInt;
    }


    public int getUserId() {
        return this.userId;
    }

    public abstract void addTrustLinkOutgoing(int otherUserId);
    public abstract Set<Integer> getTrustLinksOutgoing();
    public abstract Set<Region> getRegionsReviewed();

    public Set<Integer> getCategoriesReviewed() {
        return this.categoriesReviewed;
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
