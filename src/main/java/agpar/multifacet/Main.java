package agpar.multifacet;

import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.experiments.ExperimentRunner;
import agpar.multifacet.experiments.PCCPredictionExperimentRunner;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.RecommenderTester;
import agpar.multifacet.recommend.SoRecTester;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        RecommenderTester recommender = new SoRecTester();
        ExperimentDescription desc = new ExperimentDescription(
                "PCCPrediction",
                "SoRec",
                1000,
                2,
                1000,
                10f
        );
        String resultPath = Path.of(Settings.EXPERIMENT_DIR, desc.getName(), "results.txt").toString();
        ResultWriter writer = new SynchronizedAppendResultWriter(resultPath);
        ExperimentRunner exp = new PCCPredictionExperimentRunner(desc, recommender, writer);
        exp.run();
    }
}
