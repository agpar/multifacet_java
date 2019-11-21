package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.data_interface.yelp.collections.ReviewsById;
import agpar.multifacet.data_interface.yelp.data_classes.Review;
import agpar.multifacet.data_interface.yelp.io.IdStringToIntMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataReader {
    private String dataDir;
    private Path contentFile;
    private Path trustFile;
    private Path reviewFile;

    public DataReader(String dataDir) {
        this.dataDir = dataDir;
        this.contentFile = Paths.get(dataDir, "mc.txt");
        this.trustFile = Paths.get(dataDir, "user_rating.txt");
        this.reviewFile = Paths.get(dataDir, "rating.txt");
    }

    public ReviewsById loadReviews() {
        BufferedReader reader;
        ReviewsById reviews = new ReviewsById();
        int reviewId = 0;
        try {
            reader = new BufferedReader(new FileReader(this.reviewFile.toString()));
            String header = reader.readLine();
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
}
