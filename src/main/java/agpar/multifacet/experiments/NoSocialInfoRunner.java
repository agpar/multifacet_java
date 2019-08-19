package agpar.multifacet.experiments;

import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.exit;

public class NoSocialInfoRunner extends ExperimentRunner {


    public NoSocialInfoRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }

    protected String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = "empty.txt";
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }

    protected void generatePredictions(int numUsers) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.predictionsFilePath(numUsers)));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }
}
