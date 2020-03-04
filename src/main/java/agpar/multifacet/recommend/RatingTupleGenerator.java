package agpar.multifacet.recommend;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.collections.ReviewsById;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.yelp.YelpDataReader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.System.exit;

public class RatingTupleGenerator {

    public static void GenerateTrainReviewTuples(int userCount, String outputPath) {
        DataSet ds = DataSet.getInstance();
        ds.load(0, userCount);
        ArrayList<Review> reviewsToWrite = new ArrayList<>();

        for (User user : ds.getUsers()) {
            for(Review review : user.getReviews()) {
                reviewsToWrite.add(review);
            }
        }
        writeOut(reviewsToWrite, Path.of(outputPath, "ratings_train.txt").toString());
    }
    
    public static void GenerateTestReviewTuples(int userCount, String outputPath) {
        DataSet ds = DataSet.getInstance();
        ReviewsById reviews = ds.getTestReviews();
        ArrayList<Review> reviewsToWrite = new ArrayList<>();
        for (Collection<Review> reviewList : reviews.values()) {
            reviewsToWrite.addAll(reviewList);
        }
        writeOut(reviewsToWrite, Path.of(outputPath, "ratings_test.txt").toString());
    }

    private static void writeOut(List<Review> reviewsToWrite, String outputPath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
                for(Review review : reviewsToWrite) {
                    String line = String.format("%d %d %f\n", review.getUserId(), review.getItemId(), review.getStars());
                    writer.write(line);
                }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }
}
