package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.recommend.RatingTupleGenerator;

import java.util.List;

public class GenerateTuples implements Command{
    private String outputPath;
    private DATA_SOURCE source;

    public GenerateTuples(List<String> outputPaths, DATA_SOURCE source) {
        if(outputPaths.size() != 1) {
            throw new CommandError("Exactly one path for --genPairs is needed.");
        } else {
            this.outputPath = outputPaths.get(0);
            this.source = source;
        }
    }

    @Override
    public void runCommand() {
        System.out.printf("Generating rating tuples for librec and outputting to %s\n", outputPath);
        System.out.printf("Using %s data.\n", source.toString());
        RatingTupleGenerator.GenerateTrainReviewTuples(DEFAULT_USER_UPPER_BOUND, outputPath);
    }
}
