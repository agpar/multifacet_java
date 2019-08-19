package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.exit;

public class PCCPredictionExperimentRunner extends ExperimentRunner {


    public PCCPredictionExperimentRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }

    @Override
    protected String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("predictions_pcc_%d.txt", numUsers);
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }

    @Override
    protected void generatePredictions(int numUsers) {
        String scriptPath = Path.of(Settings.PYTHON_PROJECT_DIR, "predict_pcc.py").toString();
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
            System.out.println("Failed to generate single predictions!");
            e.printStackTrace();
            exit(1);
        }
    }
}
