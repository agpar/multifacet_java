package agpar.multifacet.data_generators;

import agpar.multifacet.data_interface.yelp.YelpData;
import agpar.multifacet.data_interface.yelp.data_classes.User;
import agpar.multifacet.pairwise.runners.PairwiseCalculator;
import agpar.multifacet.pairwise.PairwiseResult;
import agpar.multifacet.pairwise.io.ResultReader;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.pairwise.result_calculators.PCCOrNullCalculator;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.UserReviewAvgCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class FriendStrangerPCC {

    public static void generateData(String path, int userCount) {
        YelpData yd = YelpData.getInstance();
        yd.load(0, userCount);
        System.out.println("Done loading.");

        ReviewAvgCalculator avgCalculator = new UserReviewAvgCalculator(yd.getReviewsByItemId());
        ResultCalculator resultCalculator = new PCCOrNullCalculator(avgCalculator,3);
        SynchronizedAppendResultWriter writer = new SynchronizedAppendResultWriter(path);
        List<User> users = new ArrayList<User>(yd.getUsers());

        PairwiseCalculator.calc(users, resultCalculator, writer, true);
    }

    public static void averagePCC(String path) {
        try {
            List<PairwiseResult> results = new ResultReader(path).read();
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
