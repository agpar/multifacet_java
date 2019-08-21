package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.data_generators.GenerateAllPairwise;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.RecommenderTester;
import net.librec.common.LibrecException;
import net.librec.math.algorithm.Randoms;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static java.lang.System.exit;

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
        this.expDir = Path.of(Settings.EXPERIMENT_DIR, name).toString();
        this.recommender = recommender;
        this.description = description;
        this.resultWriter = resultWriter;
        if(!Files.exists(Path.of(this.expDir))) {
            throw new IOException(String.format("%s does not exist. Create it and copy any files you want to re use.", this.expDir));
        }
    }

    public ExperimentDescription getDescription() {
        return description;
    }

    public void run() {
        System.out.printf("Running %s with %d users.\n", this.name, this.description.getNumUsers());
        this.initSingleVects(this.description.getNumUsers());
        this.initPairwiseVects(this.description.getNumUsers());
        this.initPredictions(this.description.getNumUsers());
        this.initRatings(this.description.getNumUsers());
        try {
            HashMap<String, Double> results = this.evaluatePredictions(this.description.getNumUsers());
            String resultString = description.toString() + String.format("MAE: %f\nMSE: %f\n\n", results.get("MAE"), results.get("MSE"));
            resultWriter.writeResults(resultString);
        } catch (LibrecException e) {
            System.out.println("Recommender system failed.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to write results.");
            e.printStackTrace();
        }
    }

    protected HashMap<String, Double> evaluatePredictions(int numUsers) throws LibrecException {
        this.recommender.loadDescription(this.description);
        return this.recommender.learn(
                this.expDir,
                Path.of(this.ratingFilePath(numUsers)).getFileName().toString(),
                Path.of(this.predictionsFilePath(numUsers)).getFileName().toString()
        );
    };

    protected void initSingleVects(int numUsers) {
        if(!this.singleVectsExists(numUsers)) {
            System.out.println("Single vects not generated. Generating...");
            this.generateSingleVects(numUsers);
        } else {
            System.out.println("Found single vects file.");
        }
    }

    protected void initPairwiseVects(int numUsers) {
        if(!this.pairwiseVectsExists(numUsers)) {
            System.out.println("Pairwise vects not generated. Generating...");
            this.generatePairwiseVects(numUsers);
        } else {
            System.out.println("Found pairwise vects file.");
        }
    }

    protected void initPredictions(int numUsers) {
        if(!this.predictionsFileExists(numUsers)) {
            System.out.println("Predictions not generated. Generating...");
            this.generatePredictions(numUsers);
        } else {
            System.out.println("Found predictions file.");
        }
    }

    protected void initRatings(int numUsers) {
        if(!this.ratingFileExists(numUsers)) {
            System.out.println("Rating tuples not generated. Generating...");
            this.generateRating(numUsers);
        } else {
            System.out.println("Found predictions file.");
        }
    }

    protected boolean singleVectsExists(int numUsers) {
        return Files.exists(Path.of(this.singleVectFilePath(numUsers)));
    }

    protected boolean pairwiseVectsExists(int numUsers) {
        return Files.exists(Path.of(this.pairwiseVectFilePath(numUsers)));
    }

    protected boolean predictionsFileExists(int numUsers) {
        return Files.exists(Path.of(this.predictionsFilePath(numUsers)));
    }

    protected boolean ratingFileExists(int numUsers) {
        return Files.exists(Path.of(this.ratingFilePath(numUsers)));
    }

    protected String singleVectFilePath(int numUsers) {
        String singleVectFileName = String.format("single_%d.csv", numUsers);
        return Path.of(this.expDir, singleVectFileName).toString();
    }

    protected String pairwiseVectFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("pairwise_%d.csv", numUsers);
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }

    protected String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("predictions_%d.txt", numUsers);
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }

    protected String ratingFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("ratings_%d.txt", numUsers);
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }

    protected void generateSingleVects(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR, "generate_single_vects.py").toString();
        try {
            Process p = new ProcessBuilder(
                    scriptPath,
                    String.valueOf(0),
                    String.valueOf(numUsers),
                    this.singleVectFilePath(numUsers))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            int statusCode = p.waitFor();
            if(statusCode != 0) {
                throw new Exception("Failed to generate single vectors");
            }
        } catch (Exception e) {
            System.out.println("Failed to generate single vects!");
            e.printStackTrace();
            exit(1);
        }
    }

    protected void generatePairwiseVects(int numUsers) {
        GenerateAllPairwise.generateData(this.pairwiseVectFilePath(numUsers), numUsers, false);
    }

    protected void generatePredictions(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR, "predict_friendship.py").toString();
        try {
            Process p = new ProcessBuilder(
                    scriptPath,
                    this.singleVectFilePath(numUsers),
                    this.pairwiseVectFilePath(numUsers),
                    this.predictionsFilePath(numUsers))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            int statusCode = p.waitFor();
            if(statusCode != 0) {
                throw new Exception("Failed to generate predictions");
            }
        } catch (Exception e) {
            System.out.println("Failed to generate predictions!");
            e.printStackTrace();
            exit(1);
        }
    }

    protected void generateRating(int numUsers) {
        RatingTupleGenerator.GenerateReviewTuples(numUsers, this.ratingFilePath(numUsers));
    }
}
