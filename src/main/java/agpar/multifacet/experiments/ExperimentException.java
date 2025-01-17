package agpar.multifacet.experiments;

public class ExperimentException extends RuntimeException {

    public ExperimentException(String msg) {
        super(msg);
    }

    public ExperimentException(Throwable e) {
        super(e);
    }

    public ExperimentException(String msg, Throwable e) {
        super(msg, e);
    }
}
