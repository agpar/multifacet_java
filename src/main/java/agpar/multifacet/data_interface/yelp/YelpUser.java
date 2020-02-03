package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.data_interface.data_classes.Region;
import agpar.multifacet.data_interface.data_classes.User;
import com.google.gson.JsonObject;

import java.util.*;

public class YelpUser extends User {
    private HashSet<Region> regionsReviewed = new HashSet<>();
    private HashSet<Integer> friends = new HashSet<>();

    public YelpUser(int userIdInt, String trueUserId) {
        super(userIdInt, trueUserId);
    }

    public static User fromJson(JsonObject obj) {
        String userId = obj.get("true_user_id").getAsString();
        int userIdInt = obj.get("user_id").getAsInt();
        YelpUser user = new YelpUser(userIdInt, userId);

        String friendStr = obj.get("friends").getAsString();
        Collection<String> friendSplit = Arrays.asList(friendStr.split(", "));
        for(String friendId : friendSplit) {
            user.addTrustLinkOutgoing(Integer.parseInt(friendId));
        }

        return user;
    }

    public Set<Region> getRegionsReviewed() {
        return this.regionsReviewed;
    }

    @Override
    public void addTrustLinkOutgoing(int otherUserId) {
        this.friends.add(otherUserId);

    }

    @Override
    public Set<Integer> getTrustLinksOutgoing() {
        return Collections.unmodifiableSet(friends);
    }
}
