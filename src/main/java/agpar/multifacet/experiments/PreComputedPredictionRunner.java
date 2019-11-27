package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.IOException;
import java.nio.file.Path;

public class PreComputedPredictionRunner extends ExperimentRunner{
    public PreComputedPredictionRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }

    @Override
    protected String predictionsFilePath(int numUsers) {
        String pairwiseVectFileName = this.description.getPredictionFile();
        return Path.of(this.description.getExperimentDir(), pairwiseVectFileName).toString();
    }
}
