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
    private int numIterations;
    private float socialReg;
    private String predictionFile ;

    // For multi.
    private List<Float> socialRegRange;
    private Float socialRegStep;
    private List<Integer> randomSeeds;

    private double MAE;
    private double RMSE;
    private double AUC;

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
        return randomSeeds != null || socialRegRange != null ||  socialRegStep != null;
    }

    public List<ExperimentDescription> multiToList() throws Exception {
        if (!this.isMulti()) {
            throw new Exception("This descriptions is not a multi!");
        }
        if(socialRegRange.size() > 2) {
            throw new Exception("SocialRegRange must bet a list of exactly two floats (upper and lower bound)");
        }

        ArrayList<ExperimentDescription> descriptions = new ArrayList<>();
        Float f = socialRegRange.get(0);
        while (f <= socialRegRange.get(1)) {
            for (int seed : this.randomSeeds) {
                descriptions.add(new ExperimentDescription(
                        this.name,
                        this.recommenderName,
                        this.numUsers,
                        seed,
                        this.numIterations,
                        f,
                        this.predictionFile));
            }
            f += this.socialRegStep;
        }
        System.out.printf("Expanded multi for %s to %d experiments\n", this.name, descriptions.size());
        return descriptions;
    }

    public String toJson() throws Exception {
        if (this.isMulti()) {
            throw new Exception("Can't serialize a multi description");
        }
        JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(this.name));
        result.add("recommenderName", new JsonPrimitive(this.recommenderName));
        result.add("numUsers", new JsonPrimitive(this.numUsers));
        result.add("randomSeed", new JsonPrimitive(this.randomSeed));
        result.add("numIterations", new JsonPrimitive(this.numIterations));
        result.add("socialReg", new JsonPrimitive(this.socialReg));
        if (this.predictionFile != null) {
            result.add("predictionFile", new JsonPrimitive(this.predictionFile));
        }
        result.add("MAE", new JsonPrimitive(this.MAE));
        result.add("RMSE", new JsonPrimitive(this.RMSE));
        result.add("AUC", new JsonPrimitive(this.AUC));
        return result.toString();
    }

    public void addResults(double MAE, double MSE, double AUC) {
        this.MAE = MAE;
        this.RMSE = MSE;
        this.AUC = AUC;
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
