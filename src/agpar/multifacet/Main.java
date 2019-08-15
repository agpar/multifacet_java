package agpar.multifacet;

import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.SocialMFReommender;
import net.librec.common.LibrecException;

import java.nio.file.Path;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) {
        try {
            SocialMFReommender.learn(Settings.EXPERIMENT_DIR, "ratings_2000.txt", "predictions_2000.txt");
        } catch(LibrecException e) {
            e.printStackTrace();
            exit(1);
        }
//        ExperimentRunner exp = new ExperimentRunner();
//        exp.runFriendPredict("first_try", 2000);
    }
}
