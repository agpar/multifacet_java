package agpar.multifacet.recommend.recommender_testers;

import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.recommend.RecommenderTester;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.TrustMFRecommender;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TrustMFTester extends RecommenderTester {

    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);

        conf.setFloat("rec.social.regularization", description.getSocialReg());
        conf.setBoolean("rec.recommender.earlystop", true);
    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SynchronousTrustMFRecommender(this.randomSeed);
        recommender.recommend(context);

        return recommender;
    }
}

// Gets around a race condition in the setup function.
class SynchronousTrustMFRecommender extends TrustMFRecommender {
    private static Lock setupLock = new ReentrantLock();
    private ConvergenceTester convergenceTester;

    public SynchronousTrustMFRecommender(long seed) {
        this.convergenceTester = new ConvergenceTester(getClass().getSimpleName(), LOG);
        Randoms.seed(seed);
    }

    public void setup() throws LibrecException
    {
        setupLock.lock();
        super.setup();
        setupLock.unlock();
    }

    public boolean isConverged(int iter) throws LibrecException {
        return convergenceTester.isConverged(lastLoss, loss, iter);
    }
}
