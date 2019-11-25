package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.yelp.data_classes.Business;
import agpar.multifacet.data_interface.yelp.io.DataReader;
import agpar.multifacet.data_interface.yelp.collections.ReviewsById;
import agpar.multifacet.data_interface.yelp.collections.UsersById;
import agpar.multifacet.data_interface.yelp.data_classes.Review;
import agpar.multifacet.data_interface.yelp.data_classes.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class YelpData {
    private UsersById usersById;
    private ReviewsById reviewsByItemId;
    private HashMap<Integer, Business> businesses;
    private DataReader reader;
    private static YelpData instance;

    private YelpData() {
        this.reader = new DataReader(Settings.YELP_DATA_DIR());
    }

    public static YelpData getInstance() {
        if(YelpData.instance == null) {
            YelpData.instance = new YelpData();
        }
        return YelpData.instance;
    }

    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.usersById = reader.loadUsers(start, stop);
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadTrainReviews();
        System.out.println("Loading Businesses");
        this.businesses = reader.loadBusinesses();

        // Adding reviews and categories to users.
        ReviewsById reviewsByUserId = new ReviewsById();
        for (List<Review> reviews: this.reviewsByItemId.values()) {
            for (Review review: reviews) {
                reviewsByUserId.put(review.getUserIdInt(), review);
            }
        }

        for (User user : this.usersById.values()) {
            user.addReviews(reviewsByUserId.get(user.getUserIdInt()));
        }

        // Adding categories
        for(User user: this.usersById.values()) {
            for (Review review : user.getReviews()) {
                Business business = this.businesses.get(review.getItemIdInt());
                user.getCategoriesReviewed().addAll(business.categories);
            }
        }

    }

    public ReviewsById getReviewsByItemId() {
        return this.reviewsByItemId;
    }

    public Collection<User> getUsers() {
        return this.usersById.values();
    }

    public UsersById getUsersById() {
        return this.usersById;
    }
}
