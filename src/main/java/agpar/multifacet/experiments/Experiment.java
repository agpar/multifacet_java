package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.RecommenderTester;
import net.librec.common.LibrecException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public abstract class Experiment implements Runnable {
    private final static String expectedRatingFileName = "rating_tuples.txt";
    protected RecommenderTester recommender;
    protected ExperimentDescription description;
    protected ResultWriter resultWriter;


    public Experiment(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException{
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
        if (!this.predictionsFileExists())
        {
            throw new ExperimentException("Prediction file not found at expected path: " +
                    this.predictionsFilePath());
        }
        if (!this.ratingFileExists()) {
            throw new ExperimentException("Rating file not found at expected path: " +
                    this.ratingFilePath());
        }
    }

    protected boolean predictionsFileExists() {
        return Files.exists(Path.of(this.predictionsFilePath()));
    }

    protected abstract String predictionsFilePath();

    protected boolean ratingFileExists() {
        return Files.exists(Path.of(this.ratingFilePath()));
    }

    protected String ratingFilePath() {
        return Path.of(this.description.getExperimentDir(), expectedRatingFileName).toString();
    }

    private void runExperiment() {
        printExperimentLogLine();
        try {
            description.addResults(this.evaluatePredictions());
            resultWriter.writeResults(String.format("%s\n", description.toJson()));
        } catch (LibrecException e) {
            System.out.println("Recommender system failed.");
            throw new ExperimentException(e);
        } catch (IOException e) {
            System.out.println("Failed to write results.");
            throw new ExperimentException(e);
        }
    }

    private void printExperimentLogLine() {
        System.out.printf("Running %s with %d users. Seed: %d. SocialReg: %f\n", this.description.getName(),
                this.description.getNumUsers(), this.description.getRandomSeed(), this.description.getSocialReg());
    }

    protected HashMap<String, Double> evaluatePredictions() throws LibrecException {
        this.recommender.loadDescription(this.description);
        return this.recommender.learn(
                this.description.getExperimentDir(),
                this.description.getName(),
                Path.of(this.ratingFilePath()).getFileName().toString(),
                Path.of(this.predictionsFilePath()).getFileName().toString()
        );
    }
}
