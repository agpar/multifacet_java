package agpar.multifacet.experiments;

import agpar.multifacet.recommend.RecRunner;

import java.io.IOException;

public class FriendPredictionExperimentRunner extends ExperimentRunner {

    public FriendPredictionExperimentRunner(String name, RecRunner recommender, int seed) throws IOException {
        super(name, recommender, seed);
    }
}
