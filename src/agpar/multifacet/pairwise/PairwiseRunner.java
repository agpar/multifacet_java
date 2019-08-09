package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.User;
import agpar.multifacet.data_interface.review_tools.ReviewAvgCalculator;

import java.util.List;

public class PairwiseRunner implements Runnable{

    private List<User> users;
    private ReviewAvgCalculator avgCalculator;
    private int outerIndex;

    public PairwiseRunner(List<User> users, ReviewAvgCalculator avgCalculator, int outerIndex) {
        this.users = users;
        this.avgCalculator = avgCalculator;
        this.outerIndex = outerIndex;
    }

    @Override
    public void run() {
        User outerUser = users.get(outerIndex);
        for(int j = outerIndex + 1; j < users.size(); j++) {
            User innerUser = users.get(j);
            PairwiseMetrics.reviewPcc(outerUser, innerUser, avgCalculator, 3);
        }
        System.out.println(outerIndex);
    }
}
