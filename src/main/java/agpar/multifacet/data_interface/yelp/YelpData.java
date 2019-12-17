package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.List;

public class YelpData extends DataSet {

    public YelpData() {
        this.reader = new YelpDataReader(Settings.YELP_DATA_DIR());
    }

    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.usersById = reader.loadUsers(start, stop);
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadReviews();
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
}
