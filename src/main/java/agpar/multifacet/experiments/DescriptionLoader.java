package agpar.multifacet.experiments;

import agpar.multifacet.Settings;
import agpar.multifacet.pairwise.io.ResultWriter;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.RecommenderTester;
import agpar.multifacet.recommend.SoRecTester;
import agpar.multifacet.recommend.TrustSVDTester;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DescriptionLoader {
    public static String VALID_RECOMMENDERS = "SoRec, TrustSVD";
    public static String VALID_NAMES = "FriendPrediction, NoPrediction, PCCPrediction";

    public static ExperimentRunner load(ExperimentDescription description) throws Exception {
        RecommenderTester recommender = DescriptionLoader.getRecommender(description.getRecommenderName());
        String resultPath = Path.of(Settings.EXPERIMENT_DIR, description.getName(), "results.txt").toString();
        ResultWriter writer = SynchronizedAppendResultWriter.getSingleton(resultPath);
        return getRunner(description, recommender, writer);
    }

    public static List<ExperimentRunner> load(List<ExperimentDescription> descriptions) throws Exception {
        List<ExperimentRunner> runners = new ArrayList<>();
        for (ExperimentDescription description : descriptions) {
            runners.add(DescriptionLoader.load(description));
        }
        return runners;
    }

    protected static RecommenderTester getRecommender(String name) throws Exception {
        if (name.equals("SoRec")) {
            return new SoRecTester();
        } else if (name.equals("TrustSVD")) {
            return new TrustSVDTester();
        } else {
            throw new Exception(String.format("Unknown recommender name: %s. Valid recommenders are: %s",
                    name, DescriptionLoader.VALID_RECOMMENDERS));
        }
    }

    protected static ExperimentRunner getRunner(
            ExperimentDescription description,
            RecommenderTester recommender,
            ResultWriter writer) throws Exception {
        String name  = description.getName();
        if (name.equals("FriendPrediction")) {
            return new FriendPredictionExperimentRunner(description, recommender, writer);
        } else if (name.equals("NoPrediction")) {
            return new NoSocialInfoRunner(description, recommender, writer);
        } else if (name.equals("PCCPrediction")) {
            return new PCCPredictionExperimentRunner(description, recommender, writer);
        } else {
            throw new Exception(String.format("Unknown experiment name: %s. Valid names are: %s",
                    name, DescriptionLoader.VALID_NAMES));
        }
    }
}
