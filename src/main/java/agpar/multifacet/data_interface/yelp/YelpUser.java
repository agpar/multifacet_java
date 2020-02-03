package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.data_interface.data_classes.Region;
import agpar.multifacet.data_interface.data_classes.User;
import com.google.gson.JsonObject;

import java.util.*;

public class YelpUser extends User {
    private HashSet<Region> regionsReviewed = new HashSet<>();

    public YelpUser(int userIdInt, String trueUserId) {
        super(userIdInt, trueUserId);
    }

    @Override
    public Set<Region> getRegionsReviewed() {
        return regionsReviewed;
    }

    public static User fromJson(JsonObject obj) {
        String userId = obj.get("true_user_id").getAsString();
        int userIdInt = obj.get("user_id").getAsInt();
        return new YelpUser(userIdInt, userId);
    }

    public void addTrustLink(int destUser) {
        this.trustGraph.addMutualLink(getUserId(), destUser);
    }
}
