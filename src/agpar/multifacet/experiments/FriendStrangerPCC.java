package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.YelpData;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.PairwiseResult;
import agpar.multifacet.pairwise.io.ResultReader;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.pairwise.result_calculators.PCCOrNullCalculator;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise.PairwiseRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class FriendStrangerPCC {
    public static String DATA_PATH = Path.of(Settings.RAM_DATA_DIR, "friend_stranger_pcc.csv").toString();

    public static void generateData(int userCount) {
        YelpData yd = new YelpData();
        yd.load(0, userCount);
        System.out.println("Done loading.");

        ItemReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(yd.getReviewsByItemId());
        ResultCalculator resultCalculator = new PCCOrNullCalculator(avgCalculator, 3);
        SynchronizedAppendResultWriter writer = new SynchronizedAppendResultWriter(FriendStrangerPCC.DATA_PATH);
        List<User> users = new ArrayList<User>(yd.getUsers());

        ExecutorService executor = Executors.newFixedThreadPool(16);
        for (int i = 0; i < users.size(); i++) {
            PairwiseRunner runner = new PairwiseRunner(users, resultCalculator, writer, i);
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
    }

    public static void averagePCC() {
        try {
            List<PairwiseResult> results = new ResultReader(FriendStrangerPCC.DATA_PATH).read();
            FriendStrangerPCC.calcAvg(results);
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    private static void calcAvg(List<PairwiseResult> results) {
        double friend_pcc_sum = 0;
        long friend_pcc_count = 0;
        double stranger_pcc_sum = 0;
        long stranger_pcc_count = 0;
        for (PairwiseResult result : results) {
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
        double friend_avg = friend_pcc_sum / friend_pcc_count;
        double stranger_avg = stranger_pcc_sum / stranger_pcc_count;
        System.out.printf("Number of friend links: %d\n", friend_pcc_count);
        System.out.printf("Friend pcc avg: %f\n", friend_avg);
        System.out.printf("Number of stranger links: %d\n", stranger_pcc_count);
        System.out.printf("Stranger pcc avg: %f\n", stranger_avg);
    }
}
