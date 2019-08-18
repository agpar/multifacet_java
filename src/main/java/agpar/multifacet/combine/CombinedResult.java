package agpar.multifacet.combine;

import agpar.multifacet.pairwise.PairwiseResult;

public class CombinedResult {
    public PairwiseResult pairwise;
    public SoloResult truster;
    public SoloResult trustee;

    public CombinedResult(PairwiseResult pairwise, SoloResult truster, SoloResult trustee) {
        assert(pairwise.user1Id.equals(truster.userId));
        assert(pairwise.user2Id.equals(trustee.userId));
        this.pairwise = pairwise;
        this.truster = truster;
        this.trustee = trustee;
    }

}
