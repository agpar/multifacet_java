package agpar.multifacet;

import agpar.multifacet.experiments.GenerateAllPairwise;
import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.SocialMFReommender;
import net.librec.common.LibrecException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.exit;

/*
Runs an entire experiment from beginning to end.
 */
public class ExperimentRunner {

    private String expDir;

    public ExperimentRunner(String name) {
        this.expDir = Path.of(Settings.EXPERIMENT_DIR, name).toString();
        if(!Files.exists(Path.of(this.expDir))) {
            new File(this.expDir).mkdirs();
        }
    }


    public void runFriendPredict(String title, int numUsers) throws LibrecException{
        System.out.printf("Running %s with %d users.\n", title, numUsers);
        this.initSingleVects(numUsers);
        this.initPairwiseVects(numUsers);
        this.initPredictions(numUsers);
        this.initRatings(numUsers);
        this.evaluatePredictions(numUsers);
    }

    private void evaluatePredictions(int numUsers) throws LibrecException {
        SocialMFReommender.learn(
                this.expDir,
                Path.of(this.ratingFilePath(numUsers)).getFileName().toString(),
                Path.of(this.predictionsFilePath(numUsers)).getFileName().toString()
        );
    }

    private void initSingleVects(int numUsers) {
        if(!this.singleVectsExists(numUsers)) {
            System.out.println("Single vects not generated. Generating...");
            this.generateSingleVects(numUsers);
        } else {
            System.out.println("Found single vects file.");
        }
    }

    private void initPairwiseVects(int numUsers) {
        if(!this.pairwiseVectsExists(numUsers)) {
            System.out.println("Pairwise vects not generated. Generating...");
            this.generatePairwiseVects(numUsers);
        } else {
            System.out.println("Found pairwise vects file.");
        }
    }

    private void initPredictions(int numUsers) {
        if(!this.predictionsFileExists(numUsers)) {
            System.out.println("Predictions not generated. Generating...");
            this.generatePredictions(numUsers);
        } else {
            System.out.println("Found predictions file.");
        }
    }

    private void initRatings(int numUsers) {
        if(!this.ratingFileExists(numUsers)) {
            System.out.println("Rating tuples not generated. Generating...");
            this.generateRating(numUsers);
        } else {
            System.out.println("Found predictions file.");
        }
    }

    private boolean singleVectsExists(int numUsers) {
        return Files.exists(Path.of(this.singleVectFilePath(numUsers)));
    }

    private boolean pairwiseVectsExists(int numUsers) {
        return Files.exists(Path.of(this.pairwiseVectFilePath(numUsers)));
    }

    private boolean predictionsFileExists(int numUsers) {
        return Files.exists(Path.of(this.predictionsFilePath(numUsers)));
    }

    private boolean ratingFileExists(int numUsers) {
        return Files.exists(Path.of(this.ratingFilePath(numUsers)));
    }

    private String singleVectFilePath(int numUsers) {
        String singleVectFileName = String.format("single_%d.csv", numUsers);
        return Path.of(Settings.EXPERIMENT_DIR, singleVectFileName).toString();
    }

    private String pairwiseVectFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("pairwise_%d.csv", numUsers);
        return Path.of(Settings.EXPERIMENT_DIR, pairwiseVectFileName).toString();
    }

    private String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("predictions_%d.txt", numUsers);
        return Path.of(Settings.EXPERIMENT_DIR, pairwiseVectFileName).toString();
    }

    private String ratingFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("ratings_%d.txt", numUsers);
        return Path.of(Settings.EXPERIMENT_DIR, pairwiseVectFileName).toString();
    }

    private void generateSingleVects(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR, "generate_single_vects.py").toString();
        String cmd = String.format("%s 0 %d %s", scriptPath, numUsers, this.singleVectFilePath(numUsers));
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int statusCode = p.waitFor();
            if(statusCode != 0) throw new Exception("Failed to generate single vectors");
        } catch (Exception e) {
            System.out.println("Failed to generate single vects!");
            e.printStackTrace();
            exit(1);
        }
    }

    private void generatePairwiseVects(int numUsers) {
        GenerateAllPairwise.generateData(this.pairwiseVectFilePath(numUsers), numUsers, false);
    }

    private void generatePredictions(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR, "predict_friendship.py").toString();
        String cmd = String.format("%s %s %s %s",
                scriptPath,
                this.singleVectFilePath(numUsers),
                this.pairwiseVectFilePath(numUsers),
                this.predictionsFilePath(numUsers));

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int statusCode = p.waitFor();
            if(statusCode != 0) throw new Exception("Failed to generate predictions");
        } catch (Exception e) {
            System.out.println("Failed to generate single vects!");
            e.printStackTrace();
            exit(1);
        }
    }

    private void generateRating(int numUsers) {
        RatingTupleGenerator.GenerateReviewTuples(numUsers, this.ratingFilePath(numUsers));
    }
}
