package agpar.multifacet.recommend.recommender_testers;

import net.librec.common.LibrecException;
import org.apache.commons.logging.Log;

public class ConvergenceTester {

    private Log logger;
    private String recName;
    private double convergenceLimit = 5.0;

    public ConvergenceTester(String recName, Log logger) {
        this.logger = logger;
        this.recName = recName;
    }

    public boolean isConverged(double lastLoss, double loss, int iter) throws LibrecException{
        float delta_loss = (float) (lastLoss - loss);

        // print out debug info
        String info = recName + " iter " + iter + ": loss = " + loss + ", delta_loss = " + delta_loss;
        logger.info(info);

        if (Double.isNaN(loss) || Double.isInfinite(loss)) {
            throw new LibrecException("Loss = NaN or Infinity: current settings does not fit the recommender! Change the settings and try again!");
        }

        // check if converged
        return Math.abs(delta_loss) < convergenceLimit;
    }
}
