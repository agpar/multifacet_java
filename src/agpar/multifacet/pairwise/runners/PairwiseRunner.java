package agpar.multifacet.pairwise.runners;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise.PairwiseResult;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.pairwise.review_avg_calculators.ReviewAvgCalculator;
import agpar.multifacet.pairwise.result_calculators.ResultCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class PairwiseRunner implements Runnable{

    private List<User> users;
    private ResultCalculator resultCalculator;
    private ResultWriter resultWriter;
    private int outerIndex;

    public PairwiseRunner(List<User> users,
                          ResultCalculator resultCalculator,
                          ResultWriter resultWriter,
                          int outerIndex) {
        this.users = users;
        this.resultCalculator = resultCalculator;
        this.resultWriter = resultWriter;
        this.outerIndex = outerIndex;
    }

    @Override
    public void run() {
        User outerUser = users.get(outerIndex);
        List<PairwiseResult> results = new ArrayList<>(users.size() - (outerIndex + 1));
        for(int j = outerIndex + 1; j < users.size(); j++) {
            User innerUser = users.get(j);
            PairwiseResult result = resultCalculator.calc(outerUser, innerUser);
            if (result != null) {
                results.add(result);
            }
        }
        try {
            this.resultWriter.WriteResults(results);
        } catch (IOException e) {
            System.out.println("Failed to write results in thread.");
            e.printStackTrace();
            exit(1);
        }
        System.out.println(outerIndex);
    }
}
