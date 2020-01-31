package agpar.multifacet.commands;

public class PrintHelp implements Command {
    @Override
    public void runCommand() {
        System.out.println("Options are --numThread, --genPairs, --genSingle, --genTuples and --epinions");
    }
}
