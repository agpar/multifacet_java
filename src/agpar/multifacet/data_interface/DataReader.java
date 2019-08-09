package agpar.multifacet.data_interface;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataReader {
    private Path dataDir;
    private Path userFile;
    private Path reviewFile;

    public DataReader(Path dataDir) {
        this.dataDir = dataDir;
        this.userFile = Paths.get(dataDir.toString(), "user.json");
        this.reviewFile = Paths.get(dataDir.toString(), "review.json");
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

    public ReviewsById loadReviews(int start, int stop) {
        BufferedReader reader;
        ReviewsById reviews = new ReviewsById();
        try{
            reader = new BufferedReader(new FileReader(this.reviewFile.toString()));
            String line = reader.readLine();
            JsonParser parser = new JsonParser();
            int lineno = 0;
            while (line != null) {
                if (lineno >= start) {
                    JsonObject obj = parser.parse(line).getAsJsonObject();
                    Review review = Review.fromJson(obj);
                    reviews.put(review.getItemId(), review);
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
        return reviews;
    }
}
