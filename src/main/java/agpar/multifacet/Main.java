package agpar.multifacet;

import agpar.multifacet.experiments.DescriptionLoader;
import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.experiments.ExperimentRunner;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new Exception("At least one experiment description file is required.");
        }

        try {
            List<ExperimentRunner> experiments = loadExperiments(args);
            runAllExperiments(experiments);

        } finally {
            SynchronizedAppendResultWriter.closeAllSingletons();
        }
    }

    public static List<ExperimentRunner> loadExperiments(String[] files) throws Exception {
        Type DESCRIPTION_TYPE = new TypeToken<List<ExperimentDescription>>() { }.getType();
        List<ExperimentRunner> experiments = new ArrayList<>();
        for(String descriptionFile : files) {
            JsonReader reader = new JsonReader(new FileReader(descriptionFile));
            List<ExperimentDescription> descriptions = new Gson().fromJson(reader, DESCRIPTION_TYPE);
            experiments.addAll(DescriptionLoader.load(descriptions));
            reader.close();
        }
        return experiments;
    }

    public static void runAllExperiments(List<ExperimentRunner> experiments) {
        // Experiments must be groups by seed, as the seed is a global static.
        HashMap<Integer, List<ExperimentRunner>> experimentsBySeed = new HashMap<>();
        for(ExperimentRunner exp : experiments) {
            if(!experimentsBySeed.containsKey(exp.getDescription().getRandomSeed())) {
                experimentsBySeed.put(exp.getDescription().getRandomSeed(), new ArrayList<ExperimentRunner>());
            }
            experimentsBySeed.get(exp.getDescription().getRandomSeed()).add(exp);
        }

        // Run all experiments with the same seed in parallel
        for(Integer key : experimentsBySeed.keySet()) {
            List<ExperimentRunner> experimentsWithSeed = experimentsBySeed.get(key);
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (ExperimentRunner exp : experimentsWithSeed) {
               executor.execute(exp);
            }
            boolean exited;
            try {
                executor.shutdown();
                exited = executor.awaitTermination(1, TimeUnit.DAYS);
                if (!exited) {
                    throw new Exception("Executor did not terminate.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                exit(1);
            }
        }
    }
}
