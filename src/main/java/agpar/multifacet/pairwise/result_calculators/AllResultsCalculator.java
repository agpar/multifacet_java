package agpar.multifacet.pairwise.result_calculators;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise.PairwiseMetrics;
import agpar.multifacet.pairwise.PairwiseResult;

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

        PairwiseResult result =  new PairwiseResult(
                user1.getUserId(),
                user2.getUserId(),
                pcc,
                socialJacc,
                areFriends,
                socialJacc > 0,
                itemJacc);

        return result;
    }
}
