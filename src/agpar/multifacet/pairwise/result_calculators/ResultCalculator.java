package agpar.multifacet.pairwise.result_calculators;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise.PairwiseResult;

public abstract class ResultCalculator {
    protected ReviewAvgCalculator avgCalculator;
    protected int minPCCOverlap;

    public ResultCalculator(ReviewAvgCalculator avgCalculator, int minPCCOverlap) {
        this.avgCalculator = avgCalculator;
        this.minPCCOverlap = minPCCOverlap;
    }

    public abstract PairwiseResult calc(User user1, User user2);
}
