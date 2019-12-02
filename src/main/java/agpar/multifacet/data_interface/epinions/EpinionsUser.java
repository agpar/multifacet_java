package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.data_interface.data_classes.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EpinionsUser extends User {
    private HashSet<Integer> distrustedUsers;
    private HashSet<Integer> friendLinksIncoming;

    public EpinionsUser(String userId, int userIdInt, HashSet<Integer> friendsInt,
                        HashSet<Integer> distrustedUsers, HashSet<Integer> friendLinksIncoming) {
        super(userId, userIdInt, friendsInt);
        this.distrustedUsers = distrustedUsers;
        this.friendLinksIncoming = friendLinksIncoming;
    }

    public Set<Integer> getDistrustedUsers() {
        return Collections.unmodifiableSet(distrustedUsers);
    }

    public Set<Integer> getFriendsLinksIncoming() {
        return Collections.unmodifiableSet(friendLinksIncoming);
    }
}
