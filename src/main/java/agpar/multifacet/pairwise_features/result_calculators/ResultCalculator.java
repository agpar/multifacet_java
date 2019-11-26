package agpar.multifacet.pairwise_features.result_calculators;

import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise_features.PairwiseResult;

public abstract class ResultCalculator {
    protected ReviewAvgCalculator avgCalculator;
    protected UsersById usersById;
    protected int minPCCOverlap;

    public ResultCalculator(ReviewAvgCalculator avgCalculator, int minPCCOverlap) {
        this.avgCalculator = avgCalculator;
        this.minPCCOverlap = minPCCOverlap;
    }

    public abstract PairwiseResult calc(User user1, User user2);
}
