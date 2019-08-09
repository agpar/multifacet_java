package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.User;
import agpar.multifacet.data_interface.review_tools.ReviewAvgCalculator;

import java.util.ArrayList;
import java.util.List;

// TODO:: Add some way to exclude calculations to save time.
public class PairwiseRunner implements Runnable{

    private List<User> users;
    private ReviewAvgCalculator avgCalculator;
    private int outerIndex;
    private List<PairwiseResults> results;

    public PairwiseRunner(List<User> users, ReviewAvgCalculator avgCalculator, int outerIndex) {
        this.users = users;
        this.avgCalculator = avgCalculator;
        this.outerIndex = outerIndex;
    }

    @Override
    public void run() {
        User outerUser = users.get(outerIndex);
        this.results = new ArrayList<>(users.size() - (outerIndex + 1));
        for(int j = outerIndex + 1; j < users.size(); j++) {
            User innerUser = users.get(j);
            Double pcc = PairwiseMetrics.reviewPcc(outerUser, innerUser, avgCalculator, 3);
            boolean areFriends = PairwiseMetrics.areFriends(outerUser, innerUser);
            double socialJacc = PairwiseMetrics.socialJaccard(outerUser, innerUser);
            this.results.add(new PairwiseResults(
                    outerUser.getUserId(),
                    innerUser.getUserId(),
                    pcc,
                    socialJacc,
                    areFriends
            ));
        }
        System.out.println(outerIndex);
    }

    public List<PairwiseResults> getResults() {
        return this.results;
    }
}
