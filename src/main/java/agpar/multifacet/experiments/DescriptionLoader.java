package agpar.multifacet.experiments;

import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.pairwise_features.io.SynchronizedAppendResultWriter;
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
    private static final String defaultResultDir = "results";
    private static final String defaultResultFileName = "results.txt";

    public static List<Experiment> load(ExperimentDescription description) throws Exception {
        if(description.isMulti()) {
            return DescriptionLoader.load(description.multiToList());
        }

        RecommenderTester recommender = getRecommender(description.getRecommenderName());
        createResultDir(description.getExperimentDir());
        ResultWriter writer = SynchronizedAppendResultWriter.getSingleton(resultFilePath(description.getExperimentDir()));
        List<Experiment> experiments = new ArrayList<>();
        experiments.add(new Experiment(description, recommender, writer));
        return experiments;
    }

    public static List<Experiment> load(List<ExperimentDescription> descriptions) throws Exception {
        List<Experiment> experiments = new ArrayList<>();
        for (ExperimentDescription description : descriptions) {
            experiments.addAll(DescriptionLoader.load(description));
        }
        return experiments;
    }

    private static synchronized void createResultDir(String experimentDir) {
        try {
            Path resultDir = Path.of(experimentDir, defaultResultDir);
            if (!Files.exists(resultDir)) {
                Files.createDirectory(resultDir);
            }
        } catch (IOException e) {
            throw new ExperimentException("Failed to setup result directory.", e);
        }
    }

    private static String resultFilePath(String experimentDir) {
        return Path.of(experimentDir, defaultResultDir, defaultResultFileName).toString();
    }

    private static RecommenderTester getRecommender(String name) throws Exception {
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
}
