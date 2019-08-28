package agpar.multifacet.experiments;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class ExperimentDescription {
    private String name;
    private String recommenderName;
    private int numUsers;
    private int randomSeed;
    private List<Integer> randomSeeds;
    private int numIterations;
    private float socialReg;
    private List<Float> socialRegs;
    private String predictionFile;
    private double  MAE;
    private double MSE;

    public ExperimentDescription(String name, String recommenderName, int numUsers, int randomSeed, int numIterations, float socialReg) {
        this.name = name;
        this.recommenderName = recommenderName;
        this.numUsers = numUsers;
        this.randomSeed = randomSeed;
        this.numIterations = numIterations;
        this.socialReg = socialReg;
    }

    public ExperimentDescription(String name, String recommenderName, int numUsers, int randomSeed, int numIterations, float socialReg, String predictionFile) {
        this.name = name;
        this.recommenderName = recommenderName;
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

    public boolean isMulti() {
        return randomSeeds != null || socialRegs != null;
    }

    public List<ExperimentDescription> multiToList() throws Exception {
        if (!this.isMulti()) {
            throw new Exception("This descriptions is not a multi!");
        }
        if (this.randomSeeds == null || this.socialRegs == null || (this.randomSeeds.size() != this.socialRegs.size())) {
            throw new Exception("`randomSeeds` and `socialRegs` are not the same length!");
        }
        ArrayList<ExperimentDescription> descriptions = new ArrayList<>();
        for(int i = 0; i < this.randomSeeds.size(); i++) {
            descriptions.add(new ExperimentDescription(
                   this.name,
                   this.recommenderName,
                   this.numUsers,
                   this.randomSeeds.get(i),
                   this.numIterations,
                   this.socialRegs.get(i),
                   this.predictionFile
            ));
        }
        return descriptions;
    }

    public String toJson() {
        JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(this.name));
        result.add("recommenderName", new JsonPrimitive(this.recommenderName));
        result.add("numUsers", new JsonPrimitive(this.numUsers));
        result.add("randomSeed", new JsonPrimitive(this.randomSeed));
        result.add("numIterations", new JsonPrimitive(this.numIterations));
        result.add("socialReg", new JsonPrimitive(this.socialReg));
        result.add("predictionFile", new JsonPrimitive(this.predictionFile));
        result.add("MAE", new JsonPrimitive(this.MAE));
        result.add("MSE", new JsonPrimitive(this.MSE));
        return result.toString();
    }

    public void addResults(double MAE, double MSE) {
        this.MAE = MAE;
        this.MSE = MSE;
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
}
