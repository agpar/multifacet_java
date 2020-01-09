package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.*;
import agpar.multifacet.recommend.recommender_testers.SoRecTester;
import agpar.multifacet.recommend.recommender_testers.SoRegTester;
import agpar.multifacet.recommend.recommender_testers.TrustMFTester;
import agpar.multifacet.recommend.recommender_testers.TrustSVDTester;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DescriptionLoader {
    public static String VALID_RECOMMENDERS = "SoRec, TrustSVD, TrustMF";

    public static List<Experiment> load(ExperimentDescription description) throws Exception {
        if(description.isMulti()) {
            return DescriptionLoader.load(description.multiToList());
        }
        RecommenderTester recommender = DescriptionLoader.getRecommender(description.getRecommenderName());
        String resultPath = Path.of(description.getExperimentDir(), "results", "results.txt").toString();
        ResultWriter writer = SynchronizedAppendResultWriter.getSingleton(resultPath);
        List<Experiment> runner = new ArrayList<>();
        runner.add(getRunner(description, recommender, writer));
        return runner;
    }

    public static List<Experiment> load(List<ExperimentDescription> descriptions) throws Exception {
        List<Experiment> runners = new ArrayList<>();
        for (ExperimentDescription description : descriptions) {
            runners.addAll(DescriptionLoader.load(description));
        }
        return runners;
    }

    protected static RecommenderTester getRecommender(String name) throws Exception {
        if (name.equals("SoRec")) {
            return new SoRecTester();
        } else if (name.equals("TrustSVD")) {
            return new TrustSVDTester();
        } else if (name.equals("TrustMF")) {
            return new TrustMFTester();
        } else if (name.equals("SoReg")) {
            return new SoRegTester();
        }
        else {
            throw new Exception(String.format("Unknown recommender name: %s. Valid recommenders are: %s",
                    name, DescriptionLoader.VALID_RECOMMENDERS));
        }
    }

    protected static Experiment getRunner(
            ExperimentDescription description,
            RecommenderTester recommender,
            ResultWriter writer) throws Exception {
            String name  = description.getName();
            return new PreComputedPredictionsExperiment(description, recommender, writer);
    }
}
