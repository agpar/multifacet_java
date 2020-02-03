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
        double socialJacc = PairwiseMetrics.socialJaccard(user1, user2);
        boolean areFriends = PairwiseMetrics.areFriends(user1, user2);
        double itemJacc = PairwiseMetrics.itemJaccard(user1, user2);
        double categoryJacc = PairwiseMetrics.categoryJaccard(user1, user2);
        Double predictability = PairwiseMetrics.predictability(user1, user2, 1, 3);

        PairwiseResult result =  new PairwiseResult(user1.getUserId(), user2.getUserId());
        result.addResult("PCC", pcc);
        result.addResult("socialJacc", socialJacc);
        result.addResult("areFriends", areFriends);
        result.addResult("areFriendsOfFriends", socialJacc > 0);
        result.addResult("itemJacc", itemJacc);
        result.addResult("categoryJacc", categoryJacc);
        result.addResult("predictability", predictability);

        return result;
    }
}
