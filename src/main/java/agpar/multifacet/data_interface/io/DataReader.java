package agpar.multifacet.data_interface.io;

import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Business;
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

public class DataReader {
    private String dataDir;
    private Path userFile;
    private Path reviewFile;
    private Path businessFile;
    private IdStringToIntMap userIdMap;
    private IdStringToIntMap reviewIdMap;
    private IdStringToIntMap itemIdMap;
    private IdStringToIntMap categoryIdMap;


    public DataReader(String dataDir) {
        this.dataDir = dataDir;
        this.userFile = Paths.get(dataDir, "user.json");
        this.reviewFile = Paths.get(dataDir, "review_no_text.json");
        this.businessFile = Paths.get(dataDir, "business.json");
        this.userIdMap = new IdStringToIntMap();
        this.reviewIdMap = new IdStringToIntMap();
        this.itemIdMap = new IdStringToIntMap();
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
                    User user = User.fromJson(obj, this.userIdMap);
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

    public ReviewsById loadReviews() {
        BufferedReader reader;
        ReviewsById reviews = new ReviewsById();
        try{
            reader = new BufferedReader(new FileReader(this.reviewFile.toString()));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            while (line != null) {
                JsonObject obj = parser.parse(line).getAsJsonObject();
                Review review = Review.fromJson(obj, this.itemIdMap, this.userIdMap);
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
                Business business = Business.fromJson(obj, this.itemIdMap, this.categoryIdMap);
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
