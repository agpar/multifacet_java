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
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new Exception("At least one experiment description file is required.");
        }

        try {
            loadExperiments(args);
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
}
