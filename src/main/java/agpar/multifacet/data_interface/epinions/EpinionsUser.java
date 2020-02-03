package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.data_classes.Region;
import agpar.multifacet.data_interface.data_classes.User;

import java.util.Collections;
import java.util.Set;

public class EpinionsUser extends User {
    private TrustGraph distrustGraph;

    public EpinionsUser(int userIdInt) {
        super(userIdInt, String.valueOf(userIdInt));
        this.distrustGraph = TrustGraph.getDistrustGlobal();
    }

    @Override
    public void addTrustLink(int destUser) {
        this.trustGraph.addDirectedLink(userId, destUser);
    }

    public Set<Integer> getDistrustLinksIncoming() {
        return distrustGraph.getIncoming(userId);
    }

    public Set<Integer> getDistrustLinksOutgoing() {
        return distrustGraph.getOutgoing(userId);
    }

    public void addDistrustLink(int destUser) {
        distrustGraph.addDirectedLink(userId, destUser);
    }

    @Override
    public Set<Region> getRegionsReviewed() {
       return Collections.emptySet();
    }
}
