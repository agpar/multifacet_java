package agpar.multifacet.experiments;

import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.IOException;
import java.nio.file.Path;

public class PCCClusterPredictionExperimentRunner extends ExperimentRunner{
    public PCCClusterPredictionExperimentRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }

    @Override
    protected String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = String.format("predictions_pcc_cluster_%d.txt", numUsers);
        return Path.of(this.expDir, pairwiseVectFileName).toString();
    }
}
