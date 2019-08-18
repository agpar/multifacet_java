package agpar.multifacet;

import agpar.multifacet.data_interface.io.DataReader;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.Collection;
import java.util.List;

public class YelpData {
    private UsersById usersById;
    private ReviewsById reviewsByItemId;
    private DataReader reader;
    private static YelpData instance;

    private YelpData() {
        this.reader = new DataReader(Settings.RAM_DATA_DIR);
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
        this.reviewsByItemId = reader.loadReviews(0, 100000000);

        // Adding reviews to users.
        ReviewsById reviewsByUserId = new ReviewsById();
        for (List<Review> reviews: this.reviewsByItemId.values()) {
            for (Review review: reviews) {
                reviewsByUserId.put(review.getUserIdInt(), review);
            }
        }

        for (User user : this.usersById.values()) {
            user.addReviews(reviewsByUserId.get(user.getUserIdInt()));
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
