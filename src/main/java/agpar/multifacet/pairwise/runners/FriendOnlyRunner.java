package agpar.multifacet.pairwise.runners;

import agpar.multifacet.data_interface.yelp.YelpData;
import agpar.multifacet.data_interface.yelp.collections.UsersById;
import agpar.multifacet.data_interface.yelp.data_classes.User;
import agpar.multifacet.pairwise.PairwiseResult;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class FriendOnlyRunner implements Runnable {
    private List<User> users;
    private ResultCalculator resultCalculator;
    private ResultWriter resultWriter;
    private int outerIndex;

    public FriendOnlyRunner(List<User> users,
                          ResultCalculator resultCalculator,
                          ResultWriter resultWriter,
                          int outerIndex) {
        this.users = users;
        this.resultCalculator = resultCalculator;
        this.resultWriter = resultWriter;
        this.outerIndex = outerIndex;
    }

    @Override
    public void run() {
        UsersById usersById = YelpData.getInstance().getUsersById();
        User outerUser =  this.users.get(this.outerIndex);
        List<PairwiseResult> results = new ArrayList<>(users.size() - (outerIndex + 1));
        for(int friendId : outerUser.getFriendsInt()) {
            User friend = usersById.get(friendId);
            if(friend == null) {
                continue;
            }
            if(friend.getUserIdInt() < outerUser.getUserIdInt()) {
                continue;
            }
            PairwiseResult result = this.resultCalculator.calc(outerUser, friend);
            if (result == null) {
                continue;
            }
            results.add(result);
        }
        try {
            this.resultWriter.writeResults(results);
        } catch (IOException e) {
            System.out.println("Failed to write results in thread.");
            e.printStackTrace();
            exit(1);
        }
        System.out.println(outerIndex);
    }
}
