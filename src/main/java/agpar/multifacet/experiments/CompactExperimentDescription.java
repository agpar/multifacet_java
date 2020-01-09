package agpar.multifacet.experiments;

import java.util.ArrayList;
import java.util.List;

public class CompactExperimentDescription {
    private String name;
    private List<String> predictionFiles;
    private String recommenderName;
    private String experimentDir;
    private List<Float> socialRegRange;
    private Float socialRegStep;
    private List<Integer> randomSeeds;
    private int latentDim = 10;
    private int numIterations;

    public List<ExperimentDescription> expand() {
        if(socialRegRange.size() != 2) {
            throw new ExperimentException("SocialRegRange must be a list of exactly two floats (upper and lower bound)");
        }

        ArrayList<ExperimentDescription> descriptions= new ArrayList<>();
        for (String predictionFile : predictionFiles) {
            for (Float socialReg : expandSocialRegulators()) {
                for (Integer randomSeed : randomSeeds) {
                    descriptions.add(new ExperimentDescription(
                            name,
                            recommenderName,
                            experimentDir,
                            -1, //intending to remove this.
                            randomSeed,
                            numIterations,
                            socialReg,
                            predictionFile
                    ));
                }
            }
        }

        return descriptions;
    }

    private List<Float> expandSocialRegulators(){
        ArrayList<Float> socialRegulators = new ArrayList<>();
        for(float i = socialRegRange.get(0); i <= socialRegRange.get(1); i += socialRegStep) {
            socialRegulators.add(i);
        }
        return socialRegulators;
    }
}
