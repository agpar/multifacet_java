package agpar.multifacet.commands;

public interface Command {
    int DEFAULT_USER_UPPER_BOUND = Integer.MAX_VALUE;
    void runCommand();
}
