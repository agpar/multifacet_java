package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.single_features.GenerateAllSingle;

import java.util.List;

public class GenerateSingle implements Command {
    private String outputPath;
    private DATA_SOURCE source;

    public GenerateSingle(List<String> outputPaths, DATA_SOURCE source) {
        if(outputPaths.size() != 1) {
            throw new CommandError("Exactly one path for --genSingle is needed.");
        }
        else if (source == DATA_SOURCE.YELP) {
            throw new CommandError("--genSingle is only supported when --epinions is also used.");
        }
        else {
            this.outputPath = outputPaths.get(0);
            this.source = source;
        }
    }
    @Override
    public void runCommand() {
        System.out.printf("Generating single features and outputting to %s\n", outputPath);
        System.out.printf("Using %s data.\n", source.toString());
        GenerateAllSingle.generateData(outputPath);
    }
}
