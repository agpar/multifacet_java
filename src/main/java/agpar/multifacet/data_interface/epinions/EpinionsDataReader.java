package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Business;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.collections.UsersById;
import agpar.multifacet.data_interface.data_classes.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class EpinionsDataReader {
    private String dataDir;
    private Path contentFile;
    private Path trustFile;
    private Path reviewFile;

    public EpinionsDataReader(String dataDir) {
        this.dataDir = dataDir;
        this.contentFile = Paths.get(dataDir, "mc.txt");
        this.trustFile = Paths.get(dataDir, "user_rating.txt");
        this.reviewFile = Paths.get(dataDir, "rating_train.txt");
    }

    public ReviewsById loadTrainReviews() {
        BufferedReader reader;
        ReviewsById reviews = new ReviewsById();
        int reviewId = 0;
        try {
            reader = new BufferedReader(new FileReader(this.reviewFile.toString()));
            String header = reader.readLine();
            verifyReviewHeader(header);
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split(",");
                reviews.put(reviewId, new Review(
                        Integer.valueOf(reviewId).toString(),
                        Integer.parseInt(splitLine[1]),
                        Integer.parseInt(splitLine[0]),
                        splitLine[5],
                        Double.parseDouble(splitLine[2])
                ));
                reviewId++;
                line = reader.readLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return reviews;
    }

    private void verifyReviewHeader(String header) {
        String[] splitHeader = header.split(",");
        assert(splitHeader[0].equals("object_id"));
        assert(splitHeader[1].equals("user_id"));
        assert(splitHeader[2].equals("rating"));
        assert(splitHeader[5].equals("date"));
    }

    public UsersById loadUsers(ReviewsById reviewsByUserId) {
        BufferedReader reader;
        UsersById users = new UsersById();
        HashMap<String, HashMap<Integer, HashSet<Integer>>> trustLinks = loadTrustLinks();
        HashMap<Integer, HashSet<Integer>> friends = trustLinks.get("friends");
        HashMap<Integer, HashSet<Integer>> enemies = trustLinks.get("enemies");

        try {
            reader = new BufferedReader(new FileReader(this.reviewFile.toString()));
            String header = reader.readLine();
            verifyReviewHeader(header);
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split(",");
                int userId = Integer.parseInt(splitLine[1]);
                if(reviewsByUserId.get(userId).size() < 19) {
                    continue;
                }
                if (!users.containsKey(userId)) {
                    users.put(new EpinionsUser(
                            "null",
                            userId,
                            friends.getOrDefault(userId, new HashSet<>()),
                            enemies.getOrDefault(userId, new HashSet<>())
                    ));
                }
                line = reader.readLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return users;
    }

    private HashMap<String, HashMap<Integer, HashSet<Integer>>> loadTrustLinks() {
        BufferedReader reader;
        HashMap<Integer, HashSet<Integer>> friends = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> enemies = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(this.trustFile.toFile()));
            String header = reader.readLine();
            verifyTrustHeader(header);
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split(",");
                int trustPolarity = Integer.parseInt(splitLine[2]);
                int trusterId = Integer.parseInt(splitLine[0]);
                int trusteeId = Integer.parseInt(splitLine[1]);
                if (trustPolarity > 0) {
                    friends.computeIfAbsent(trusterId, k -> new HashSet<>());
                    friends.get(trusterId).add(trusteeId);
                } else {
                   enemies.computeIfAbsent(trusterId, k -> new HashSet<>());
                   enemies.get(trusterId).add(trusteeId);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return new HashMap<>() {{
            put("friends", friends);
            put("enemies", enemies);
        }};
    }

    public HashMap<Integer, Business> loadBusinesses() {
        BufferedReader reader;
        HashMap<Integer, Business> businesses = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(this.reviewFile.toFile()));
            String header = reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] splitLine = line.split(",");
                Business business = new Business(Integer.parseInt(splitLine[0]), new HashSet<Integer>());
                businesses.put(business.itemId, business);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return businesses;
    }

    private void verifyTrustHeader(String header) {
        String[] splitHeader = header.split(",");
        assert(splitHeader[0].equals("user_id"));
        assert(splitHeader[1].equals("other_id"));
        assert(splitHeader[2].equals("rating"));
        assert(splitHeader[3].equals("date"));
    }


}
