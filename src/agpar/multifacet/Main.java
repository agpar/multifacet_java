package agpar.multifacet;

import agpar.multifacet.recommend.SocialMFReommender;
import net.librec.common.LibrecException;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws Exception {
        ExperimentRunner exp = new ExperimentRunner("OnlyFriendPredictions");
        exp.runFriendPredict(2000);
    }
}
