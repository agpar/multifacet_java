package agpar.multifacet;

import agpar.multifacet.data_interface.review_tools.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise.PairwiseRunner;
import agpar.multifacet.data_interface.DataReader;
import agpar.multifacet.data_interface.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        Path dataDir = Path.of("/home/aparment/Documents/datasets/yelp");
        DataReader dl = new DataReader(dataDir);
        YelpData yd = new YelpData(dl);
        yd.load(0, 6000);
        System.out.println("Done loading.");

        // Test some pcc calculations
        ItemReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(yd.getReviewsByItemId());
        List<User> users = new ArrayList<User>(yd.getUsers());
        ExecutorService executor = Executors.newFixedThreadPool(16);
        for(int i = 0; i < users.size(); i++) {
            PairwiseRunner runner = new PairwiseRunner(users, avgCalculator, i);
            executor.execute(runner);
        }
        executor.shutdown();
    }
}
