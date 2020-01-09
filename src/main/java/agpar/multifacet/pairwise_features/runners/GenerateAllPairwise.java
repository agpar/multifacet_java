package agpar.multifacet.pairwise_features.runners;


import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.result_calculators.RelevantConnectionsCalculator;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.ResultCalculator;
import agpar.multifacet.pairwise_features.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise_features.review_avg_calculators.UserReviewAvgCalculator;

import java.util.ArrayList;
import java.util.List;

public class GenerateAllPairwise {
    public static void generateData(String path, int userCount, boolean printProgress) {
        DataSet data = DataSet.getInstance();
        data.load(0, userCount);

        ReviewAvgCalculator avgCalculator = new UserReviewAvgCalculator(data.getReviewsByItemId());
        ResultCalculator resultCalculator = new RelevantConnectionsCalculator(avgCalculator, 3);
        ResultWriter writer = new ResultWriter(path);
        List<User> users = new ArrayList<User>(data.getUsers());

        PairwiseCalculator.calc(users, resultCalculator, writer, printProgress);
    }
}
