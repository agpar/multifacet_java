package agpar.multifacet.commands;

public class CommandError extends RuntimeException {
    public CommandError(String message) {
        super(message);
    }
}
