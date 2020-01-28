package agpar.multifacet.data_interface;


import agpar.multifacet.Main;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.epinions.EpinionsData;
import agpar.multifacet.data_interface.yelp.YelpDataReader;
import agpar.multifacet.data_interface.yelp.YelpData;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class DataSet {
    protected UsersById usersById;
    protected ReviewsById reviewsByItemId;
    protected HashMap<Integer, Business> businesses;
    protected YelpDataReader reader;
    private static DataSet instance;

    public abstract void load(int start, int stop);

    public static DataSet getInstance() {
        if (instance != null) {
            return instance;
        }

        switch(Main.get_source()) {
            case YELP :
                instance = new YelpData();
                break;
            case EPINIONS:
                instance = new EpinionsData();
                break;
            default:
                System.out.println("Data set not yet implemented");
                System.exit(1);
                break;
        }
        return instance;
    }

    public ReviewsById getReviewsByItemId() {
        return this.reviewsByItemId;
    }

    public Collection<User> getUsers() {
        return this.usersById.values();
    }

    public UsersById getUsersById() {
        return this.usersById;
    }

    public HashMap<Integer, Business> getBussiness() {
        return this.businesses;
    }
}
