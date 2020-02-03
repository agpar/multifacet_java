package agpar.multifacet.pairwise_features.runners;

import agpar.multifacet.MockedDataSet;
import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.epinions.EpinionsUser;
import agpar.multifacet.data_interface.yelp.YelpUser;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.AllResultsCalculator;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.mockito.Mockito.mock;

public class RelevantPairwiseRunnerTests {

    MockedDataSet data;
    RelevantPairwiseRunner runner;

    @Before
    public void setup() {
        data = new MockedDataSet();
        runner = new RelevantPairwiseRunner(
                new ArrayList<>(data.dataset.getUsers()),
                mock(AllResultsCalculator.class),
                mock(ResultWriter.class),
                1,
                false
        );
        TrustGraph.resetGlobals();
    }

    @Test
    public void mutual_friends_are_compared() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        user1.addTrustLink(user2.getUserId());
        user2.addTrustLink(user1.getUserId());
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void nonmutual_outgoing_friends_are_compared() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        user1.addTrustLink(user2.getUserId());
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void nonmutual_incoming_friends_are_not_compared() {
        User user1 = new EpinionsUser(1);
        User user2 = new EpinionsUser(2);
        user2.addTrustLink(user1.getUserId());
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.size() == 0);
    }

    @Test
    public void users_not_compared_to_themselves() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        user1.addTrustLink(user1.getUserId());
        user2.addTrustLink(user2.getUserId());
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(!relevantUsers.contains(user1));
    }

    @Test
    public void users_who_trust_a_middle_user_are_compared() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");
        User user3 = new YelpUser(3, "id");
        user1.addTrustLink(user2.getUserId());
        user3.addTrustLink(user2.getUserId());

        data.registerUsers(Arrays.asList(user1, user2, user3));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user3));
    }

    @Test
    public void user_who_have_reviewed_the_same_item_are_compared() {
        User user1 = new YelpUser(1, "id");
        Review r1 = new Review(1, 3, "", 0);
        user1.addReviews(Arrays.asList(r1));
        User user2 = new YelpUser(2, "id");
        Review r2 = new Review(2, 3, "", 0);
        user2.addReviews(Arrays.asList(r2));

        data.registerUsers(Arrays.asList(user1, user2));
        data.registerReviews(Arrays.asList(r1, r2));

        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void users_who_have_no_relation_are_not_compared() {
        User user1 = new YelpUser(1, "id");
        User user2 = new YelpUser(2, "id");

        data.registerUsers(Arrays.asList(user1, user2));

        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.size() == 0);
    }
}