package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DescriptionLoader {
    public static String VALID_RECOMMENDERS = "SoRec, TrustSVD, TrustMF";

    public static List<ExperimentRunner> load(ExperimentDescription description) throws Exception {
        if(description.isMulti()) {
            return DescriptionLoader.load(description.multiToList());
        }
        RecommenderTester recommender = DescriptionLoader.getRecommender(description.getRecommenderName());
        String resultPath = Path.of(description.getExperimentDir(), description.getName(), "results.txt").toString();
        ResultWriter writer = SynchronizedAppendResultWriter.getSingleton(resultPath);
        List<ExperimentRunner> runner = new ArrayList<>();
        runner.add(getRunner(description, recommender, writer));
        return runner;
    }

    public static List<ExperimentRunner> load(List<ExperimentDescription> descriptions) throws Exception {
        List<ExperimentRunner> runners = new ArrayList<>();
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

    protected static ExperimentRunner getRunner(
            ExperimentDescription description,
            RecommenderTester recommender,
            ResultWriter writer) throws Exception {
            String name  = description.getName();
            return new PreComputedPredictionRunner(description, recommender, writer);
    }
}
