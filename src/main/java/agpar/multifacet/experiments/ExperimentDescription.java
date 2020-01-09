package agpar.multifacet.experiments;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExperimentDescription {
    private String name;
    private String recommenderName;
    private String expDir;
    private int numUsers;
    private int randomSeed;
    private int numIterations;
    private float socialReg;
    private String predictionFile;
    private int latentDim = 10;

    private HashMap<String, Double> results;

    public ExperimentDescription(String recommenderName, String expDir, int numUsers, int randomSeed, int numIterations, float socialReg, String predictionFile) {
        this.name = predictionFile.split("\\.")[0];
        this.recommenderName = recommenderName;
        this.expDir = expDir;
        this.numUsers = numUsers;
        this.randomSeed = randomSeed;
        this.numIterations = numIterations;
        this.socialReg = socialReg;
        this.predictionFile = predictionFile;
    }

    public String toString() {
        String template = "Experiment: %s\nRecommender: %s\nNumUsers: %d\nSeed: %d\nIters: %d\nSocialReg: %f\n";
        return String.format(template, this.name, this.recommenderName, this.numUsers, this.randomSeed, this.numIterations, this.socialReg);
    }

    public String toJson() {
        JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(this.name));
        result.add("recommenderName", new JsonPrimitive(this.recommenderName));
        result.add("numUsers", new JsonPrimitive(this.numUsers));
        result.add("randomSeed", new JsonPrimitive(this.randomSeed));
        result.add("numIterations", new JsonPrimitive(this.numIterations));
        result.add("socialReg", new JsonPrimitive(this.socialReg));
        result.add("latentDim", new JsonPrimitive(this.latentDim));
        result.add("predictionFile", new JsonPrimitive(this.predictionFile));

        for (String key : this.results.keySet()) {
            result.add(key, new JsonPrimitive(this.results.get(key)));
        }
        return result.toString();
    }

    public void addResults(HashMap<String, Double> results) {
        this.results = results;
    }

    public String getName() {
        return name;
    }

    public String getRecommenderName() {
        return recommenderName;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public float getSocialReg() {
        return socialReg;
    }
    public String getPredictionFile() {
        return predictionFile;
    }

    public String getExperimentDir() {
        return this.expDir;
    }

    public int getLatentDim() {
        if (latentDim == 0) {
            return 10;
        }
        else {
            return latentDim;
        }
    }
}
