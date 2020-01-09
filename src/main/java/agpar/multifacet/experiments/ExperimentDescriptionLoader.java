package agpar.multifacet.experiments;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ExperimentDescriptionLoader {
    public static List<ExperimentDescription> loadFromFile(String filePath) {
        JsonReader reader;
        CompactExperimentDescription compactDescription;
        Type DESCRIPTION_TYPE = new TypeToken<CompactExperimentDescription>() { }.getType();
        try {
            reader = new JsonReader(new FileReader(filePath));
            compactDescription = new Gson().fromJson(reader, DESCRIPTION_TYPE);
            reader.close();
        } catch (IOException e) {
            throw new ExperimentException("Failed to load experiment descriptions from " + filePath, e);
        }
        return compactDescription.expand();
    }
}
