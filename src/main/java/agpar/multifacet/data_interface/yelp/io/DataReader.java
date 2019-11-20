package agpar.multifacet.data_interface.yelp.io;

import agpar.multifacet.data_interface.yelp.collections.UsersById;
import agpar.multifacet.data_interface.yelp.collections.ReviewsById;
import agpar.multifacet.data_interface.yelp.data_classes.Business;
import agpar.multifacet.data_interface.yelp.data_classes.Review;
import agpar.multifacet.data_interface.yelp.data_classes.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DataReader {
    private String dataDir;
    private Path userFile;
    private Path reviewTrainFile;
    private Path reviewTestFile;
    private Path businessFile;
    private IdStringToIntMap categoryIdMap;


    public DataReader(String dataDir) {
        this.dataDir = dataDir;
        this.userFile = Paths.get(dataDir, "user.json");
        this.reviewTrainFile = Paths.get(dataDir, "review_train.json");
        this.reviewTestFile = Paths.get(dataDir, "review_test.json");
        this.businessFile = Paths.get(dataDir, "business.json");
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
                    User user = User.fromJson(obj);
                    users.put(user);
                }
                line = reader.readLine();
                lineno += 1;
                if (lineno >= stop) {
                    break;
                }
            }
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
                reviews.put(review.getItemIdInt(), review);
                line = reader.readLine();
            }
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
                businesses.put(business.itemId, business);
                line = reader.readLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return businesses;
    }
}
