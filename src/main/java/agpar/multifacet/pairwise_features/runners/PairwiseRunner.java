package agpar.multifacet.pairwise_features.runners;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.PairwiseResult;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.ResultCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class PairwiseRunner implements Runnable{

    private List<User> users;
    private ResultCalculator resultCalculator;
    private ResultWriter resultWriter;
    private int outerIndex;
    private boolean printProgress;

    public PairwiseRunner(List<User> users,
                          ResultCalculator resultCalculator,
                          ResultWriter resultWriter,
                          int outerIndex,
                          boolean printProgress) {
        this.users = users;
        this.resultCalculator = resultCalculator;
        this.resultWriter = resultWriter;
        this.outerIndex = outerIndex;
        this.printProgress = printProgress;
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
            this.resultWriter.writeResults(results);
        } catch (IOException e) {
            System.out.println("Failed to write results in thread.");
            e.printStackTrace();
            exit(1);
        }
        if (this.printProgress) {
            System.out.println(outerIndex);
        }
    }
}
