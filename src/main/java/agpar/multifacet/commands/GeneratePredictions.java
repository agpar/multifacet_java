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

    // TODO refactor this function.
    private void runAllExperimentsParallel(List<Experiment> experiments) {
        // Experiments must be grouped by seed, as the seed is a global static.
        // TODO deal with fact that this is no longer true.
        HashMap<Integer, List<Experiment>> experimentsBySeed = new HashMap<>();
        for (Experiment exp : experiments) {
            if (!experimentsBySeed.containsKey(exp.getDescription().getRandomSeed())) {
                experimentsBySeed.put(exp.getDescription().getRandomSeed(), new ArrayList<Experiment>());
            }
            experimentsBySeed.get(exp.getDescription().getRandomSeed()).add(exp);
        }

        // It's more efficient to run multiple experiments with the same social matrix at the same time.
        HashMap<String, List<Experiment>> experimentsBySocial = new HashMap<>();
        for (Experiment exp : experiments) {
            if (!experimentsBySocial.containsKey(exp.getDescription().getPredictionFile())) {
                experimentsBySocial.put(exp.getDescription().getPredictionFile(), new ArrayList<Experiment>());
            }
            experimentsBySocial.get(exp.getDescription().getPredictionFile()).add(exp);
        }

        // Run all experiments with the same seed and social data set in parallel
        for (String socialFile : experimentsBySocial.keySet()) {
            for (Integer seed : experimentsBySeed.keySet()) {
                List<Experiment> experimentsInSet = new ArrayList<>(experimentsBySeed.get(seed));
                experimentsInSet.retainAll(experimentsBySocial.get(socialFile));

                ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                for (Experiment exp : experimentsInSet) {
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
            // clear social data matrix before moving to next set of experiments
            SharedDataModel.resetSocial();
        }
    }
}
