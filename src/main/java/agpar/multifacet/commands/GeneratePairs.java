package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.pairwise_features.runners.GenerateAllPairwise;

import java.util.List;

public class GeneratePairs implements Command {
    private String outputPath;
    private DATA_SOURCE source;

    public GeneratePairs(List<String> outputPaths, DATA_SOURCE source) {
        if(outputPaths.size() != 1) {
            throw new CommandError("Exactly one path for --genPairs is needed.");
        } else {
            this.outputPath = outputPaths.get(0);
            this.source = source;
        }
    }

    @Override
    public void runCommand() {
        System.out.printf("Generating pairs and outputting to %s\n", outputPath);
        System.out.printf("Using %s data.\n", source.toString());
        GenerateAllPairwise.generateData(outputPath, DEFAULT_USER_UPPER_BOUND, false);
    }
}
