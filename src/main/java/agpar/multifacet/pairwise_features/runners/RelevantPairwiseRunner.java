package agpar.multifacet.pairwise_features.runners;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.PairwiseResult;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.ResultCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.lang.System.exit;

public class RelevantPairwiseRunner implements Runnable{
    private List<User> users;
    private ResultCalculator resultCalculator;
    private ResultWriter resultWriter;
    private int userIndex;
    private boolean printProgress;

    public RelevantPairwiseRunner(List<User> users,
                          ResultCalculator resultCalculator,
                          ResultWriter resultWriter,
                          int userIndex,
                          boolean printProgress) {
        this.users = users;
        this.resultCalculator = resultCalculator;
        this.resultWriter = resultWriter;
        this.userIndex = userIndex;
        this.printProgress = printProgress;
    }

    @Override
    public void run() {
        User baseUser = users.get(this.userIndex);
        HashSet<User> usersToCompareTo = findRelevantUsers(baseUser, DataSet.getInstance());
        List<PairwiseResult> results = new ArrayList<>(usersToCompareTo.size());
        for (User relevantUser : usersToCompareTo) {
            PairwiseResult result = resultCalculator.calc(baseUser, relevantUser);
            if (result != null) {
                results.add(result);
            }
        }
        writeResults(results);
    }

    public HashSet<User> findRelevantUsers(User baseUser, DataSet loadedData) {
        // Find the set of users to compare this users to.
        UsersById allUsers = loadedData.getUsersById();
        ReviewsById allReviews = loadedData.getReviewsByItemId();
        HashSet<User> usersToCompareTo = new HashSet<>();

        // Connect up the outgoing friends.
        for (Integer friendID : baseUser.getTrustLinksOutgoing()) {
            if (allUsers.containsKey(friendID)) {
                User friend = allUsers.get(friendID);
                usersToCompareTo.add(friend);

                // Connect up users who also have the same friend (socialJacc > 0)
                for (Integer alsoTrustsFriend : friend.getTrustLinksOutgoing()) {
                    if (allUsers.containsKey(alsoTrustsFriend)) {
                        usersToCompareTo.add(allUsers.get(alsoTrustsFriend));
                    }
                }
            }
        }

        // Connect up users who have reviewed an item this user has reviewed.
        for (Review review : baseUser.getReviews()) {
            for (Review reviewOfSameItem : allReviews.get(review.getItemId())) {
                if (reviewOfSameItem.getUserId() != baseUser.getUserId() &&
                        allUsers.containsKey(reviewOfSameItem.getUserId())) {
                    usersToCompareTo.add(allUsers.get(reviewOfSameItem.getUserId()));
                }
            }
        }
        usersToCompareTo.remove(baseUser);
        return usersToCompareTo;
    }

    private void writeResults(List<PairwiseResult> results) {
        try {
            this.resultWriter.writeResults(results);
        } catch (IOException e) {
            System.out.println("Failed to write results in thread.");
            e.printStackTrace();
            exit(1);
        }
        if (this.printProgress) {
            System.out.println(this.userIndex);
        }
    }
}
