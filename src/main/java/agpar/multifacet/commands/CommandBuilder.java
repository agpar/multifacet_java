package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;

import java.util.ArrayList;

public class CommandBuilder {
    private String[] args;
    private ArrayList<String> flags = new ArrayList<>();
    private ArrayList<String> files = new ArrayList<>();
    private static String[] knownFlags = {"-h", "--help", "--numThreads", "--genPairs", "--genSingle", "--genTuples", "--genStats", "--epinions"};

    public CommandBuilder(String[] cmdLindArgs) {
        args = cmdLindArgs;
        if(args.length == 0) {
            throw new CommandError("At least one experiment description file or flag is required.");
        }
        parseArgs(args);
    }

    public Command build() throws CommandError{
        return buildCommand();
    }

    public DATA_SOURCE dataSource() {
        if (flags.contains("--epinions"))
            return DATA_SOURCE.EPINIONS;
        else
            return DATA_SOURCE.YELP;
    }

    private void parseArgs(String[] args) {
        for (var arg : args) {
            if (arg.startsWith("-")) {
                addFlag(arg);
            } else {
                files.add(arg);
            }
        }
    }

    private void addFlag(String flag) {
        if (!flagIsKnown(flag))
            throw new CommandError("Unknown flag: " + flag);
        flags.add(flag);
    }

    private boolean flagIsKnown(String flag) {
        for (var knownFlag : knownFlags) {
            if (flag.startsWith(knownFlag))
                return true;
        }
        return false;
    }

    private Command buildCommand() {
        if (flags.contains("-h") || flags.contains("--help")) {
            return new PrintHelp();
        }
        if (anyFlagStartsWith("--genPairs")) {
            return new GeneratePairs(files, dataSource());
        }
        if (anyFlagStartsWith("--genTuples")) {
            return new GenerateTuples(files, dataSource());
        }
        if (anyFlagStartsWith("--genSingle")) {
            return new GenerateSingle(files, dataSource());
        }
        if (anyFlagStartsWith("--genStats")){
            return new GenerateStats(dataSource());
        }
        // If no above flag is supplied, try to start a prediction task.
        if (files.size() > 0) {
            var command = new GeneratePredictions(files);
            Integer threads = numThreads();
            if (threads != null)
                command.setNumThreads(threads);
            return command;
        }

        throw new CommandError("No flags or files were specified. Exiting.");
    }

    private boolean anyFlagStartsWith(String prefix) {
        for (var flag : flags) {
            if (flag.startsWith(prefix))
                return true;
        }
        return false;
    }

    private Integer numThreads() {
        for (var flag : flags) {
            if (flag.startsWith("--numThreads")) {
                return Integer.parseInt(flag.split("=")[1]);
            }
        }
        return null;
    }
}
