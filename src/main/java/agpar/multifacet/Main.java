package agpar.multifacet;

public class Main {

    public static void main(String[] args) throws Exception {
        ExperimentRunner exp = new ExperimentRunner("OnlyFriendPredictions");
        exp.runFriendPredict(2000);
    }
}
