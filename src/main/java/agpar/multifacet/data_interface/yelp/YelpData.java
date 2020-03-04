package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.List;

public class YelpData extends DataSet {
    private YelpDataReader reader;

    public YelpData() {
        this.reader = new YelpDataReader(Settings.YELP_DATA_DIR());
    }

    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.usersById = reader.loadUsers(start, stop);
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadTrainReviews();
        System.out.println("Loading Businesses");
        this.businesses = reader.loadBusinesses();
        System.out.println("Loading Trust");
        reader.loadTrust();
        this.trust = TrustGraph.getTrustGlobal();
        this.distrust = new TrustGraph();

        // Adding reviews and categories to users.
        ReviewsById reviewsByUserId = new ReviewsById();
        for (List<Review> reviews: this.reviewsByItemId.values()) {
            for (Review review: reviews) {
                reviewsByUserId.put(review.getUserId(), review);
            }
        }

        for (User user : this.usersById.values()) {
            user.addReviews(reviewsByUserId.get(user.getUserId()));
        }

        // Adding categories and regions
        for (User user: this.usersById.values()) {
            YelpUser yuser = (YelpUser) user;
            var userCategories = yuser.getCategoriesReviewed();
            var userRegions = yuser.getRegionsReviewed();
            for (Review review : user.getReviews()) {
                Business business = this.businesses.get(review.getItemId());
                userCategories.addAll(business.categories);
                var region = business.getRegion();
                if (region != null) {
                    userRegions.add(region);
                }
            }
        }
    }

    @Override
    public ReviewsById getTestReviews() {
        return this.reader.loadTestReviews();
    }

}
