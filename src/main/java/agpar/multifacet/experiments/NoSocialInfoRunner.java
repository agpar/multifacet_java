package agpar.multifacet.experiments;

import agpar.multifacet.pairwise.PairwiseResult;
import agpar.multifacet.recommend.RecRunner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import static java.lang.System.exit;

public class NoSocialInfoRunner extends ExperimentRunner {
    public NoSocialInfoRunner(String name, RecRunner recommender, int seed) throws IOException {
        super(name, recommender, seed);
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
