package agpar.multifacet;

import agpar.multifacet.experiments.ExperimentRunner;
import agpar.multifacet.experiments.PCCPredictionExperimentRunner;
import agpar.multifacet.recommend.RecRunner;
import agpar.multifacet.recommend.SoRecRunner;

public class Main {

    public static void main(String[] args) throws Exception {
        RecRunner recommender = new SoRecRunner();
        ExperimentRunner exp = new PCCPredictionExperimentRunner("PCCPredictions", recommender, 2);
        exp.run(1000);
    }
}
