package agpar.multifacet.pairwise_features.result_calculators;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise_features.PairwiseMetrics;
import agpar.multifacet.pairwise_features.PairwiseResult;

public class AllResultsCalculator extends ResultCalculator{


    public AllResultsCalculator(ReviewAvgCalculator avgCalculator, int minPCCOverlap) {
        super(avgCalculator, minPCCOverlap);
    }

    @Override
    public PairwiseResult calc(User user1, User user2) {
        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, this.avgCalculator, this.minPCCOverlap);
        boolean areFriends = PairwiseMetrics.areFriends(user1, user2);
        double socialJacc = PairwiseMetrics.socialJaccard(user1, user2);
        double itemJacc = PairwiseMetrics.itemJaccard(user1, user2);
        double categoryJacc = PairwiseMetrics.categoryJaccard(user1, user2);

        PairwiseResult result =  new PairwiseResult(
                user1.getUserIdInt(),
                user2.getUserIdInt(),
                pcc,
                socialJacc,
                areFriends,
                socialJacc > 0,
                itemJacc,
                categoryJacc);

        return result;
    }
}
