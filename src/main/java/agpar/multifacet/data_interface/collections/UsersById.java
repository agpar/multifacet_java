package agpar.multifacet.data_interface.collections;

import agpar.multifacet.data_interface.data_classes.User;

import java.util.Collection;
import java.util.HashMap;

public class UsersById {
    private HashMap<Integer, User> users = new HashMap<>();

    public void put(User user) {
        this.users.put(user.getUserIdInt(), user);
    }

    public User get(int userId) {
        return this.users.get(userId);
    }

    public boolean containsKey(int userId) {
        return this.users.containsKey(userId);
    }

    public Collection<User> values() {
        return this.users.values();
    }
}
