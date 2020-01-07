package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
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
    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
    /*
    The important hyper parameter here is rec.rate.social.regularization as this controls the level
    of impact the social graph has on the optimization.
    Social graph should have entries in (0, 1], where a 0 indicates an unknown.
     */

        // build data model
        DataModel dataModel = new SharedDataModel(conf);
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

    public SynchronousTrustMFRecommender(long seed) {
        Randoms.seed(seed);
    }

    public void setup() throws LibrecException
    {
        setupLock.lock();
        super.setup();
        setupLock.unlock();
    }
}
