package agpar.multifacet.experiments;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ExperimentDescription {
    private String name;
    private String recommenderName;
    private int numUsers;
    private int randomSeed;
    private int numIterations;
    private float socialReg;
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

    public String toString() {
        String template = "Experiment: %s\nRecommender: %s\nNumUsers: %d\nSeed: %d\nIters: %d\nSocialReg: %f\n";
        return String.format(template, this.name, this.recommenderName, this.numUsers, this.randomSeed, this.numIterations, this.socialReg);
    }

    public ExperimentDescription fromJson(JsonObject obj) {
        return new ExperimentDescription(
                obj.get("name").getAsString(),
                obj.get("recommenderName").getAsString(),
                obj.get("numUsers").getAsInt(),
                obj.get("randomSeed").getAsInt(),
                obj.get("numIterations").getAsInt(),
                obj.get("socialReg").getAsInt()
        );
    }

    public String toJson() {
        JsonObject result = new JsonObject();
        result.add("name", new JsonPrimitive(this.name));
        result.add("recommenderName", new JsonPrimitive(this.recommenderName));
        result.add("numUsers", new JsonPrimitive(this.numUsers));
        result.add("randomSeed", new JsonPrimitive(this.randomSeed));
        result.add("numIterations", new JsonPrimitive(this.numIterations));
        result.add("socialReg", new JsonPrimitive(this.socialReg));
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
}
