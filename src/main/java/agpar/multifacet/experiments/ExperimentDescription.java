package agpar.multifacet.experiments;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExperimentDescription {
    private String experimentName;
    private String recommenderName;
    private String expDir;
    private int randomSeed;
    private int numIterations;
    private float socialReg;
    private String predictionFile;
    private int latentDim = 10;

    private HashMap<String, Double> results;

    public ExperimentDescription(String experimentName, String recommenderName, String expDir, int randomSeed, int numIterations, float socialReg, String predictionFile) {
        this.experimentName = experimentName;
        this.recommenderName = recommenderName;
        this.expDir = expDir;
        this.randomSeed = randomSeed;
        this.numIterations = numIterations;
        this.socialReg = socialReg;
        this.predictionFile = predictionFile;
    }

    public String toString() {
        String template = "Predictor: %s\nRecommender: %s\nSeed: %d\nIters: %d\nSocialReg: %f\n";
        return String.format(template, this.getPredictorName(), this.recommenderName, this.randomSeed, this.numIterations, this.socialReg);
    }

    public String toJson() {
        JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(this.experimentName));
        result.add("recommenderName", new JsonPrimitive(this.recommenderName));
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

    public String getExperimentName() {
        return experimentName;
    }

    public String getPredictorName(){
        return predictionFile.split("\\.")[0];
    }

    public String getRecommenderName() {
        return recommenderName;
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
