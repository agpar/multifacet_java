package agpar.multifacet.pairwise.runners;


import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.review_avg_calculators.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise.runners.PairwiseCalculator;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.pairwise.result_calculators.AllResultsCalculator;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;

import java.util.ArrayList;
import java.util.List;

public class GenerateAllPairwise {
    public static void generateData(String path, int userCount, boolean printProgress) {
        DataSet data = DataSet.getInstance();
        data.load(0, userCount);

        ReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(data.getReviewsByItemId());
        ResultCalculator resultCalculator = new AllResultsCalculator(avgCalculator, 3);
        SynchronizedAppendResultWriter writer = new SynchronizedAppendResultWriter(path);
        List<User> users = new ArrayList<User>(data.getUsers());

        PairwiseCalculator.calc(users, resultCalculator, writer, printProgress);
    }
}
