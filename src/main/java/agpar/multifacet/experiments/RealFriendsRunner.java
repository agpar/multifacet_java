package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.exit;

public class RealFriendsRunner extends ExperimentRunner{

    public RealFriendsRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }

    protected void generatePredictions(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR(), "actual_friendship.py").toString();
        try {
            Process p = new ProcessBuilder(
                    scriptPath,
                    this.singleVectFilePath(numUsers),
                    this.pairwiseVectFilePath(numUsers),
                    this.predictionsFilePath(numUsers))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            int statusCode = p.waitFor();
            if (statusCode != 0) throw new Exception("Failed to generate predictions");
        } catch (Exception e) {
            System.out.println("Failed to generate single predictions!");
            e.printStackTrace();
            exit(1);
        }
    }
}
