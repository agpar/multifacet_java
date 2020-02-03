package agpar.multifacet.data_interface.data_classes;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.ReviewList;
import agpar.multifacet.data_interface.collections.TrustGraph;
import com.google.gson.JsonObject;

import java.util.*;

public abstract class User {
    private String trueUserId;
    protected int userId;
    private ReviewList reviews = new ReviewList(new ArrayList<>());
    private HashSet<Integer> categoriesReviewed = new HashSet<Integer>();
    protected TrustGraph trustGraph;

    public User(int userIdInt, String trueUserId) {
        this.trueUserId = trueUserId;
        this.userId = userIdInt;
        this.trustGraph = TrustGraph.getTrustGlobal();
    }

    public Set<Integer> getTrustLinksIncoming() {
        return trustGraph.getIncoming(userId);
    }

    public Set<Integer> getTrustLinksOutgoing() {
        return trustGraph.getOutgoing(userId);
    }

    public abstract void addTrustLink(int destUser);

    public int getUserId() {
        return this.userId;
    }

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

    public abstract Set<Region> getRegionsReviewed();
}
