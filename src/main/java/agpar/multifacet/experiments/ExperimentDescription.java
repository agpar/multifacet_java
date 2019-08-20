package agpar.multifacet.experiments;

import com.google.gson.JsonObject;

public class ExperimentDescription {
    private String name;
    private String recommenderName;
    private int numUsers;
    private int randomSeed;
    private int numIterations;
    private float socialReg;

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
