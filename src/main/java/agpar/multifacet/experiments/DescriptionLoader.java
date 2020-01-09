package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.*;
import agpar.multifacet.recommend.recommender_testers.SoRecTester;
import agpar.multifacet.recommend.recommender_testers.SoRegTester;
import agpar.multifacet.recommend.recommender_testers.TrustMFTester;
import agpar.multifacet.recommend.recommender_testers.TrustSVDTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DescriptionLoader {
    public static String VALID_RECOMMENDERS = "SoRec, TrustSVD, TrustMF";
    private static final String defaultResultFileName = "results.txt";

    public static List<Experiment> load(ExperimentDescription description) {
        RecommenderTester recommender = getRecommender(description.getRecommenderName());
        List<Experiment> experiments = new ArrayList<>();
        ResultWriter writer = setupResultWriter(description);
        experiments.add(new Experiment(description, recommender, writer));
        return experiments;
    }

    public static List<Experiment> load(List<ExperimentDescription> descriptions) {
        List<Experiment> experiments = new ArrayList<>();
        for (ExperimentDescription description : descriptions) {
            experiments.addAll(DescriptionLoader.load(description));
        }
        return experiments;
    }

    private static ResultWriter setupResultWriter(ExperimentDescription description) {
        createResultDir(description);
        return ResultWriter.getSingleton(resultFilePath(description).toString());
    }

    private static synchronized void createResultDir(ExperimentDescription description) {
        try {
            Path resultDir = resultDir(description);
            if (!Files.exists(resultDir)) {
                Files.createDirectory(resultDir);
            }
        } catch (IOException e) {
            throw new ExperimentException("Failed to setup result directory.", e);
        }
    }

    private static Path resultFilePath(ExperimentDescription description) {
        return Path.of(resultDir(description).toString(), defaultResultFileName);
    }

    private static Path resultDir(ExperimentDescription description) {
        return Path.of(description.getExperimentDir(), description.getExperimentName());
    }

    private static RecommenderTester getRecommender(String name) {
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
            throw new ExperimentException(String.format("Unknown recommender name: %s. Valid recommenders are: %s",
                    name, DescriptionLoader.VALID_RECOMMENDERS));
        }
    }
}
