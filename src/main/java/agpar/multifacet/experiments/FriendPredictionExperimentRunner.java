package agpar.multifacet.experiments;

import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.recommend.RecommenderTester;

import java.io.IOException;

public class FriendPredictionExperimentRunner extends ExperimentRunner {


    public FriendPredictionExperimentRunner(ExperimentDescription description, RecommenderTester recommender, ResultWriter resultWriter) throws IOException {
        super(description, recommender, resultWriter);
    }
}
