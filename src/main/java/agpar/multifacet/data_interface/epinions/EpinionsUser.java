package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.data_interface.data_classes.User;

import java.util.HashSet;

public class EpinionsUser extends User {
    private HashSet<Integer> distrustedUsers;

    public EpinionsUser(String userId, int userIdInt, HashSet<Integer> friendsInt, HashSet<Integer> distrustedUsers) {
        super(userId, userIdInt, friendsInt);
        this.distrustedUsers = distrustedUsers;
    }

    public HashSet<Integer> getDistrustedUsers() {
        return distrustedUsers;
    }
}
