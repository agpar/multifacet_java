package agpar.multifacet.pairwise_features.result_calculators;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise_features.PairwiseMetrics;
import agpar.multifacet.pairwise_features.PairwiseResult;

public class PCCOrNullCalculator extends ResultCalculator {

    public PCCOrNullCalculator(ReviewAvgCalculator avgCalculator, int minPCCOverlap) {
        super(avgCalculator, minPCCOverlap);
    }

    @Override
    public PairwiseResult calc(User user1, User user2) {

        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, this.avgCalculator, this.minPCCOverlap);
        if (pcc == null) {
            return null;
        }
        boolean areFriends = PairwiseMetrics.areFriends(user1, user2);
        return new PairwiseResult(
                user1.getUserIdInt(),
                user2.getUserIdInt(),
                pcc,
                0,
                areFriends,
                false,
                0,
                0
        );
    }
}