package agpar.multifacet.data_interface.yelp;

import agpar.multifacet.data_interface.collections.TrustGraph;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.data_classes.Location;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class YelpDataReader {
    private String dataDir;
    private Path userFile;
    private Path reviewTrainFile;
    private Path reviewTestFile;
    private Path businessFile;
    private IdStringToIntMap categoryIdMap;


    public YelpDataReader(String dataDir) {
        this.dataDir = dataDir;
        this.userFile = Paths.get(dataDir, "user_filtered.json");
        this.reviewTrainFile = Paths.get(dataDir, "review_train_filtered.json");
        this.reviewTestFile = Paths.get(dataDir, "review_train_filtered.json");
        this.businessFile = Paths.get(dataDir, "business_filtered.json");
        this.categoryIdMap = new IdStringToIntMap();
    }

    public UsersById loadUsers(int start, int stop) {
        BufferedReader reader;
        UsersById users = new UsersById();
        try{
            reader = new BufferedReader(new FileReader(this.userFile.toString()));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            int lineno = 0;
            while (line != null) {
                if (lineno >= start) {
                    JsonObject obj =  parser.parse(line).getAsJsonObject();
                    User user = YelpUser.fromJson(obj);
                    users.put(user);
                }
                line = reader.readLine();
                lineno += 1;
                if (lineno >= stop) {
                    break;
                }
            }
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return users;
    }

    public ReviewsById loadTrainReviews() {
        return loadReviews(this.reviewTrainFile.toString());
    }

    public ReviewsById loadTestReviews() {
        return loadReviews(this.reviewTestFile.toString());
    }

    private ReviewsById loadReviews(String path) {
        BufferedReader reader;
        ReviewsById reviews = new ReviewsById();
        try{
            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            while (line != null) {
                JsonObject obj = parser.parse(line).getAsJsonObject();
                Review review = Review.fromJson(obj);
                reviews.put(review.getItemId(), review);
                line = reader.readLine();
            }
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return reviews;
    }

    public HashMap<Integer, Business> loadBusinesses() {
        BufferedReader reader;
        HashMap<Integer, Business> businesses = new HashMap<>();
        try{
            reader = new BufferedReader(new FileReader(this.businessFile.toString()));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            while (line != null) {
                JsonObject obj = parser.parse(line).getAsJsonObject();
                Business business = Business.fromJson(obj, categoryIdMap);
                business.setLocation(Location.fromJson(obj));
                businesses.put(business.itemId, business);
                line = reader.readLine();
            }
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return businesses;
    }

    public void loadTrust() {
        BufferedReader reader;
        var trustGraph = TrustGraph.getTrustGlobal();
        try {
            reader = new BufferedReader(new FileReader(this.userFile.toString()));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            while (line != null) {
                JsonObject obj = parser.parse(line).getAsJsonObject();
                Integer userId = obj.get("user_id").getAsInt();
                String friends = obj.get("friends").getAsString();
                String[] friendSplit = friends.split(", ");
                for (var friend : friendSplit) {
                    trustGraph.addMutualLink(userId, Integer.parseInt(friend));
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
