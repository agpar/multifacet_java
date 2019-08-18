package agpar.multifacet.experiments;

import agpar.multifacet.YelpData;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.result_calculators.AllResultsCalculator;
import agpar.multifacet.pairwise.runners.FriendOnlyCalculator;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;

import java.util.ArrayList;
import java.util.List;

public class GenerateFriendsOnly {
    public static void generateData(String path, int userCount) {
        YelpData yd = YelpData.getInstance();
        yd.load(0, userCount);
        System.out.println("Done loading.");

        ReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(yd.getReviewsByItemId());
        ResultCalculator resultCalculator = new AllResultsCalculator(avgCalculator,3);
        SynchronizedAppendResultWriter writer = new SynchronizedAppendResultWriter(path);
        List<User> users = new ArrayList<User>(yd.getUsers());

        FriendOnlyCalculator.calc(users, resultCalculator, writer);
    }
}
