package agpar.multifacet.pairwise_features.runners;

import agpar.multifacet.MockedDataSet;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.io.SynchronizedAppendResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.AllResultsCalculator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
                mock(SynchronizedAppendResultWriter.class),
                1,
                false
        );
    }

    @Test
    public void mutual_friends_are_compared() {
        User user1 = new User("id", 1, new HashSet<>(Arrays.asList(2)));
        User user2 = new User("id", 2, new HashSet<>(Arrays.asList(1)));
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void nonmutual_outgoing_friends_are_compared() {
        User user1 = new User("id", 1, new HashSet<>(Arrays.asList(2)));
        User user2 = new User("id", 2, new HashSet<>(Arrays.asList()));
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void nonmutual_incoming_friends_are_not_compared() {
        User user1 = Mockito.spy(new User("id", 1, new HashSet<>()));
        Mockito.doReturn(new HashSet<>(Arrays.asList(2))).when(user1).getFriendsLinksIncoming();
        User user2 = new User("id", 2, new HashSet<>(Arrays.asList(1)));
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.size() == 0);
    }

    @Test
    public void users_not_compared_to_themselves() {
        User user1 = new User("id", 1, new HashSet<>(Arrays.asList(1, 2)));
        User user2 = new User("id", 2, new HashSet<>(Arrays.asList(1, 2)));
        data.registerUsers(Arrays.asList(user1, user2));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(!relevantUsers.contains(user1));
    }

    @Test
    public void users_who_trust_a_middle_user_are_compared() {
        User user1 = new User("id", 1, new HashSet<>(Arrays.asList(2)));
        User user2 = Mockito.spy(new User("id", 2, new HashSet<>(Arrays.asList())));
        Mockito.doReturn(new HashSet<>(Arrays.asList(1, 3))).when(user2).getFriendsLinksIncoming();
        User user3 = new User("id", 3, new HashSet<>(Arrays.asList(2)));

        data.registerUsers(Arrays.asList(user1, user2, user3));
        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user3));
    }

    @Test
    public void user_who_have_reviewed_the_same_item_are_compared() {
        User user1 = new User("id", 1, new HashSet<>());
        Review r1 = new Review("r1", 1, 3, "", 0);
        user1.addReviews(Arrays.asList(r1));
        User user2 = new User("id", 2, new HashSet<>());
        Review r2 = new Review("r2", 2, 3, "", 0);
        user2.addReviews(Arrays.asList(r2));

        data.registerUsers(Arrays.asList(user1, user2));
        data.registerReviews(Arrays.asList(r1, r2));

        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.contains(user2));
    }

    @Test
    public void users_who_have_no_relation_are_not_compared() {
        User user1 = new User("id", 1, new HashSet<>());
        User user2 = new User("id", 2, new HashSet<>());

        data.registerUsers(Arrays.asList(user1, user2));

        HashSet<User> relevantUsers = runner.findRelevantUsers(user1, data.dataset);
        assert(relevantUsers.size() == 0);
    }
}