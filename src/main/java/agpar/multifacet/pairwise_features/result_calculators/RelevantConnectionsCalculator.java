package agpar.multifacet.pairwise_features.result_calculators;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.PairwiseMetrics;
import agpar.multifacet.pairwise_features.PairwiseResult;
import agpar.multifacet.pairwise_features.review_avg_calculators.ReviewAvgCalculator;

/*
Only returns a result when the pair of users have either a item-reviewed or social overlap
or are friends.
The idea being that if they have no friends in common and have never reviewed a common item,
then there is essentially no basis to expect these users to know each other, or have any relevance
for recommending items to each other.
 */
public class RelevantConnectionsCalculator extends ResultCalculator {

    public RelevantConnectionsCalculator(ReviewAvgCalculator avgCalculator, int minPCCOverlap) {
        super(avgCalculator, minPCCOverlap);
    }

    @Override
    public PairwiseResult calc(User user1, User user2) {
        double socialJacc = PairwiseMetrics.socialJaccard(user1, user2);
        double itemJacc = PairwiseMetrics.itemJaccard(user1, user2);
        boolean areFriends = PairwiseMetrics.areFriends(user1, user2);
        if (socialJacc == 0 && itemJacc == 0 && (!areFriends)) {
            return null;
        }
        Double pcc = PairwiseMetrics.reviewPcc(user1, user2, this.avgCalculator, this.minPCCOverlap);
        double categoryJacc = PairwiseMetrics.categoryJaccard(user1, user2);
        return new PairwiseResult(
                user1.getUserIdInt(),
                user2.getUserIdInt(),
                pcc,
                socialJacc,
                areFriends,
                socialJacc > 0,
                itemJacc,
                categoryJacc
        );
    }
}
