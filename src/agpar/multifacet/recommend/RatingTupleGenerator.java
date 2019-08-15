package agpar.multifacet.recommend;

import agpar.multifacet.YelpData;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.System.exit;

public class RatingTupleGenerator {

    public static void GenerateReviewTuples(int userCount, String outputPath) {
        YelpData yd = YelpData.getInstance();
        yd.load(0, userCount);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
            for (User user : yd.getUsers()) {
                for(Review review : user.getReviews()) {
                    String line = String.format("%s %s %f\n", user.getUserId(), review.getItemId(), review.getStars());
                    writer.write(line);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }


    }
}
