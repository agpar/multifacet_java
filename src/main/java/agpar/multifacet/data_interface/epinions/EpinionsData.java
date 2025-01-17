package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.List;

public class EpinionsData extends DataSet {
    private EpinionsDataReader reader;

    public EpinionsData() {
        this.reader = new EpinionsDataReader(Settings.EPINIONS_DATA_DIR());
    }

    @Override
    public void load(int start, int stop) {
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadReviews();
        // Adding reviews and categories to users.
        ReviewsById reviewsByUserId = new ReviewsById();
        for (List<Review> reviews: this.reviewsByItemId.values()) {
            for (Review review : reviews) {
                reviewsByUserId.put(review.getUserId(), review);
            }
        }

        System.out.println("Loading Users");
        this.usersById = reader.loadUsers(reviewsByUserId);

        System.out.println("Loading Businesses");
        this.businesses = reader.loadBusinesses();

        System.out.println("Loading trust");
        reader.loadTrustLinks();
        this.trust = TrustGraph.getTrustGlobal();
        this.distrust = TrustGraph.getDistrustGlobal();

        for (User user : this.usersById.values()) {
            user.addReviews(reviewsByUserId.get(user.getUserId()));
        }

        // Adding categories
        for(User user: this.usersById.values()) {
            for (Review review : user.getReviews()) {
                Business business = this.businesses.get(review.getItemId());
                user.getCategoriesReviewed().addAll(business.categories);
            }
        }
    }

    @Override
    public ReviewsById getTestReviews() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
