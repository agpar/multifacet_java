package agpar.multifacet.data_interface;

import java.util.Collection;
import java.util.HashMap;

public class UsersById {
    private HashMap<String, User> users = new HashMap<>();

    public void put(User user) {
        this.users.put(user.getUserId(), user);
    }

    public User get(String userId) {
        return this.users.get(userId);
    }

    public boolean containsKey(String userId) {
        return this.users.containsKey(userId);
    }

    public Collection<User> values() {
        return this.users.values();
    }
}
