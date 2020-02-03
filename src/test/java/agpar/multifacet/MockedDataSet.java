package agpar.multifacet;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedDataSet {
    public DataSet dataset;
    private UsersById users;
    private ReviewsById reviews;

    public MockedDataSet() {
        users = new UsersById();
        reviews = new ReviewsById();
        dataset = mock(DataSet.class);
        when(dataset.getUsersById()).thenReturn(users);
        when(dataset.getUsers()).thenReturn(users.values());
        when(dataset.getReviewsByItemId()).thenReturn(reviews);
    }

    public void registerUsers(List<User> userList) {
        for (User user : userList) {
            users.put(user);
        }
    }

    public void registerReviews(List<Review> reviewList) {
        for (Review review : reviewList) {
            reviews.put(review.getItemId(), review);
        }
    }
}
