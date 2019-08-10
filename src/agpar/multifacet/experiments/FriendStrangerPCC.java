package agpar.multifacet.experiments;

import agpar.multifacet.YelpData;
import agpar.multifacet.data_interface.User;
import agpar.multifacet.data_interface.review_tools.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise.PairwiseResults;
import agpar.multifacet.pairwise.PairwiseRunner;
import agpar.multifacet.pairwise.ResultWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class FriendStrangerPCC {
    public static void runTest(int userCount) {
        YelpData yd = new YelpData();
        yd.load(0, userCount);
        System.out.println("Done loading.");

        ItemReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(yd.getReviewsByItemId());
        List<User> users = new ArrayList<User>(yd.getUsers());

        ExecutorService executor = Executors.newFixedThreadPool(16);
        PairwiseRunner[] runners = new PairwiseRunner[users.size()];
        for(int i = 0; i < users.size(); i++) {
            PairwiseRunner runner = new PairwiseRunner(users, avgCalculator, i);
            runners[i] = runner;
            executor.execute(runner);
        }
        boolean exited;
        try {
            executor.shutdown();
            exited = executor.awaitTermination(1000, TimeUnit.SECONDS);
            if (!exited) {
                throw new Exception("Executor did not terminate.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }

        // As a test, get average PCC between friends and non friends
        double friend_pcc_sum = 0;
        long friend_pcc_count = 0;
        double stranger_pcc_sum = 0;
        long stranger_pcc_count = 0;
        ArrayList<PairwiseResults> allResults = new ArrayList<PairwiseResults>();
        for (PairwiseRunner runner : runners) {
            for (PairwiseResults result : runner.getResults()) {
                allResults.add(result);
                if (result.pcc == null) {
                    continue;
                }
                if (result.areFriends) {
                    friend_pcc_sum += result.pcc;
                    friend_pcc_count++;
                } else {
                    stranger_pcc_sum += result.pcc;
                    stranger_pcc_count++;
                }
            }
        }
        ResultWriter.WriteResults(allResults);
        double friend_avg = friend_pcc_sum / friend_pcc_count;
        double stranger_avg = stranger_pcc_sum / stranger_pcc_count;
        System.out.printf("Friend pcc avg: %f\n", friend_avg);
        System.out.printf("Stranger pcc avg: %f\n", stranger_avg);


    }
}
