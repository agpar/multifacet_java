package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.RecommenderTester;
import net.librec.common.LibrecException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/*
Runs an entire experiment from beginning to end.
 */
public abstract class ExperimentRunner implements Runnable {

    protected String expDir;
    protected String name;
    protected RecommenderTester recommender;
    protected ExperimentDescription description;
    protected ResultWriter resultWriter;

    public ExperimentRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException{
        this.name = description.getName();
        this.expDir = Path.of(description.getExperimentDir(), name).toString();
        this.recommender = recommender;
        this.description = description;
        this.resultWriter = resultWriter;
        if(!Files.exists(Path.of(this.expDir))) {
            Files.createDirectory(Path.of(this.expDir));
        }
    }

    public ExperimentDescription getDescription() {
        return description;
    }

    public void run() {
        System.out.printf("Running %s with %d users. Seed: %d. SocialReg: %f\n", this.name,
                this.description.getNumUsers(), this.description.getRandomSeed(), this.description.getSocialReg());
        if (!this.predictionsFileExists(this.description.getNumUsers()))
        {
            System.out.println("Prediction file not found. See readme.md on how to generate a prediction file.");
            System.exit(1);
        }
        this.initRatings(this.description.getNumUsers());
        try {
            HashMap<String, Double> results = this.evaluatePredictions(this.description.getNumUsers());
            description.addResults(results);
            String resultString = description.toJson();
            resultWriter.writeResults(String.format("%s\n", resultString));
        } catch (LibrecException e) {
            System.out.println("Recommender system failed.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to write results.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Failed to write results.");
            e.printStackTrace();
        }
    }

    protected HashMap<String, Double> evaluatePredictions(int numUsers) throws LibrecException {
        this.recommender.loadDescription(this.description);
        return this.recommender.learn(
                this.description.getExperimentDir(),
                this.name,
                Path.of(this.ratingFilePath(numUsers)).getFileName().toString(),
                Path.of(this.predictionsFilePath(numUsers)).getFileName().toString()
        );
    };


    protected void initRatings(int numUsers) {
        if(!this.ratingFileExists(numUsers)) {
            System.out.println("Rating tuples not generated. Generating...");
            this.generateRating(numUsers);
        } else {
            System.out.println("Found ratings file.");
        }
    }


    protected boolean predictionsFileExists(int numUsers) {
        return Files.exists(Path.of(this.predictionsFilePath(numUsers)));
    }

    protected boolean ratingFileExists(int numUsers) {
        return Files.exists(Path.of(this.ratingFilePath(numUsers)));
    }

    protected abstract  String predictionsFilePath(int numUsers);

    protected String ratingFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("ratings_%d.txt", numUsers);
        return Path.of(this.description.getExperimentDir(), pairwiseVectFileName).toString();
    }

    protected void generateRating(int numUsers) {
        RatingTupleGenerator.GenerateTrainReviewTuples(numUsers, this.ratingFilePath(numUsers));
    }
}
