package agpar.multifacet;

import agpar.multifacet.pairwise.runners.GenerateAllPairwise;
import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.experiments.DescriptionLoader;
import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.experiments.ExperimentRunner;
import agpar.multifacet.pairwise.io.SynchronizedAppendResultWriter;
import agpar.multifacet.recommend.SharedDataModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;


public class Main {

    private static int NUM_EXPERIMENTS = Runtime.getRuntime().availableProcessors();
    private static boolean GENERATE_PAIRS = false;
    private static DATA_SOURCE SOURCE = DATA_SOURCE.YELP;
    private static boolean GENERATE_SINGLE = false;
    private static boolean PRINT_HELP = false;

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new Exception("At least one experiment description file or flag is required.");
        }

        ArrayList<String> flags = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                flags.add(arg);
            } else {
                files.add(arg);
            }
        }

        try {
            setFlags(flags);
            if (PRINT_HELP) {
                System.out.println("Options are --numThread, --genPairs, --genSingle and --epinions");
                System.exit(0);
            }
            if (GENERATE_PAIRS) {
                if(files.size() == 0) {
                    System.out.println("An output path for --genPairs is needed.");
                    System.exit(1);
                }
                System.out.printf("Generating pairs and outputting to %s\n", files.get(0));
                System.out.printf("Using %s data.\n", SOURCE.toString());
                GenerateAllPairwise.generateData(files.get(0), 500_000_000, false);
                System.exit(0);
            }

            if (GENERATE_SINGLE) {
                if(files.size() == 0) {
                    System.out.println("An output path for --genSingle is needed.");
                    System.exit(1);
                }

                if (SOURCE != DATA_SOURCE.EPINIONS) {
                    System.out.println("--genSingle is only supported when --epinions is also used.");
                    System.exit(1);
                }

                // Generate the single vects for epinions.
            }

            // If no other flag is supplied, try to start a prediction task.
            if (files.size() > 0) {
                List<ExperimentRunner> experiments = loadExperiments(files);
                runAllExperiments(experiments);
            } else {
                System.out.println("No flags or files were specified. Exiting.");
                System.exit(1);
            }
        } finally {
            SynchronizedAppendResultWriter.closeAllSingletons();
        }
    }

    public static void setFlags(List<String> flags) {
        for(String flag : flags) {
            if (flag.startsWith("--numthreads")) {
                NUM_EXPERIMENTS = Integer.parseInt(flag.split("=")[1]);
            }
            else if (flag.startsWith("--genPairs")) {
                GENERATE_PAIRS = true;
            }
            else if (flag.startsWith("--genSingle")) {
                GENERATE_SINGLE = true;
            }
            else if (flag.equals("-h") || flag.equals("help")) {
                PRINT_HELP = true;
            }
            else if (flag.startsWith("--epinions")) {
                SOURCE = DATA_SOURCE.EPINIONS;
            }
            else {
                System.out.printf("Unknown flag %s\n", flag);
                exit(1);
            }
        }
    }

    public static List<ExperimentRunner> loadExperiments(List<String> files) throws Exception {
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
        // Experiments must be grouped by seed, as the seed is a global static.
        HashMap<Integer, List<ExperimentRunner>> experimentsBySeed = new HashMap<>();
        for (ExperimentRunner exp : experiments) {
            if (!experimentsBySeed.containsKey(exp.getDescription().getRandomSeed())) {
                experimentsBySeed.put(exp.getDescription().getRandomSeed(), new ArrayList<ExperimentRunner>());
            }
            experimentsBySeed.get(exp.getDescription().getRandomSeed()).add(exp);
        }

        // It's more efficient to run multiple experiments with the same social matrix at the same time.
        HashMap<String, List<ExperimentRunner>> experimentsBySocial = new HashMap<>();
        for (ExperimentRunner exp : experiments) {
            if (!experimentsBySocial.containsKey(exp.getDescription().getPredictionFile())) {
                experimentsBySocial.put(exp.getDescription().getPredictionFile(), new ArrayList<ExperimentRunner>());
            }
            experimentsBySocial.get(exp.getDescription().getPredictionFile()).add(exp);
        }

        // Run all experiments with the same seed and social data set in parallel
        for (String socialFile : experimentsBySocial.keySet()) {
            for (Integer seed : experimentsBySeed.keySet()) {
                List<ExperimentRunner> experimentsInSet = new ArrayList<>(experimentsBySeed.get(seed));
                experimentsInSet.retainAll(experimentsBySocial.get(socialFile));

                ExecutorService executor = Executors.newFixedThreadPool(NUM_EXPERIMENTS);
                for (ExperimentRunner exp : experimentsInSet) {
                    executor.execute(exp);
                }

                boolean exited;
                try {
                    executor.shutdown();
                    exited = executor.awaitTermination(7, TimeUnit.DAYS);
                    if (!exited) {
                        throw new Exception("Executor did not terminate.");
                    }
                    SynchronizedAppendResultWriter.flushAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    exit(1);
                } finally {
                    // clear data split before next random seed assignment.
                    SharedDataModel.resetSplit();
                }
            }
            // clear social data matrix before moving to next set of experiments
            SharedDataModel.resetSocial();
        }
    }

    public static DATA_SOURCE get_source() {
        return Main.SOURCE;
    }
}
