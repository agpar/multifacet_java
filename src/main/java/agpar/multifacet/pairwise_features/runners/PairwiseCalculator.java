package agpar.multifacet.pairwise_features.runners;

import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.result_calculators.ResultCalculator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class PairwiseCalculator {
    public static void calc(List<User> users, ResultCalculator resultCalculator, ResultWriter writer, boolean printProgress) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < users.size(); i++) {
            RelevantPairwiseRunner runner = new RelevantPairwiseRunner(users, resultCalculator, writer, i, printProgress);
            executor.execute(runner);
        }
        boolean exited;
        try {
            executor.shutdown();
            exited = executor.awaitTermination(1, TimeUnit.DAYS);
            if (!exited) {
                throw new Exception("Executor did not terminate.");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
}
