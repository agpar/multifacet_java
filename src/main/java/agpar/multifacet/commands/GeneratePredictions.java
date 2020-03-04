package agpar.multifacet.commands;

import agpar.multifacet.experiments.Experiment;
import agpar.multifacet.experiments.ExperimentBuilder;
import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.experiments.ExperimentDescriptionLoader;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class GeneratePredictions implements Command {
    private int numThreads = Runtime.getRuntime().availableProcessors();
    private List<String> inputPaths;

    public GeneratePredictions(List<String> inputPaths) {
        this.inputPaths = inputPaths;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public void runCommand() {
        List<Experiment> experiments = readExperiments();
        runAllExperimentsParallel(experiments);
    }

    private List<Experiment> readExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        for(String descriptionFile : inputPaths) {
            List<ExperimentDescription> descriptions = ExperimentDescriptionLoader.loadFromFile(descriptionFile);
            experiments.addAll(ExperimentBuilder.build(descriptions));
        }
        return experiments;
    }

    private void runAllExperimentsParallel(List<Experiment> experiments) {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for (Experiment exp : experiments) {
                executor.execute(exp);
            }

            boolean exited;
            try {
                executor.shutdown();
                exited = executor.awaitTermination(7, TimeUnit.DAYS);
                if (!exited) {
                    throw new Exception("Executor did not terminate.");
                }
                ResultWriter.flushAll();
            } catch (Exception e) {
                e.printStackTrace();
                exit(1);
            }
    }
}
