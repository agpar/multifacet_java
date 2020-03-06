package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;
import net.librec.common.LibrecException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Experiment implements Runnable {
    private final static String expectedRatingTrainFileName = "ratings_train.txt";
    private final static String expectedRatingTestFileName = "ratings_test.txt";
    private RecommenderTester recommender;
    private ExperimentDescription description;
    private ResultWriter resultWriter;

    public Experiment(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) {
        this.recommender = recommender;
        this.description = description;
        this.resultWriter = resultWriter;
    }

    public ExperimentDescription getDescription() {
        return description;
    }

    public void run() {
        checkForNecessaryInputFiles();
        runExperiment();
    }

    private void checkForNecessaryInputFiles() {
        if (!Files.exists(this.predictionsFilePath()))
        {
            throw new ExperimentException("Prediction file not found at expected path: " +
                    this.predictionsFilePath());
        }
        if (!Files.exists(this.ratingTrainFilePath())) {
            throw new ExperimentException("Rating train file not found at expected path: " +
                    this.ratingTrainFilePath());
        }
        if (!Files.exists(this.ratingTestFilePath())) {
            throw new ExperimentException("Rating test file not found at expected path: " +
                    this.ratingTestFilePath());
        }
    }

    private Path predictionsFilePath() {
        return Path.of(this.description.getExperimentDir(), description.getPredictionFile());
    };

    private Path ratingTrainFilePath() {
        return Path.of(this.description.getExperimentDir(), expectedRatingTrainFileName);
    }

    private Path ratingTestFilePath() {
        return Path.of(this.description.getExperimentDir(), expectedRatingTestFileName);
    }

    private void runExperiment() {
        printExperimentLogLine();
        try {
            description.addResults(this.evaluatePredictions());
            resultWriter.writeResults(String.format("%s\n", description.toJson()));
            resultWriter.flush();
        } catch (LibrecException e) {
            System.out.println("Recommender system failed.");
            throw new ExperimentException(e);
        } catch (IOException e) {
            System.out.println("Failed to write results.");
            throw new ExperimentException(e);
        }
    }

    private void printExperimentLogLine() {
        System.out.printf("Running %s. Seed: %d. SocialReg: %f\n", this.description.getPredictorName(),
                this.description.getRandomSeed(), this.description.getSocialReg());
    }

    private HashMap<String, Double> evaluatePredictions() throws LibrecException {
        this.recommender.loadDescription(this.description);
        return this.recommender.learn(
                this.description.getExperimentDir(),
                this.description.getExperimentName(),
                this.ratingTrainFilePath().getFileName().toString(),
                this.ratingTestFilePath().getFileName().toString(),
                this.predictionsFilePath().getFileName().toString()
        );
    }
}
