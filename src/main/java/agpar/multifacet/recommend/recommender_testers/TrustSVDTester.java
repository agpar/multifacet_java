package agpar.multifacet.recommend.recommender_testers;

import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.recommend.RecommenderTester;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.TrustSVDRecommender;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TrustSVDTester extends RecommenderTester {
    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);

        // Set the social regulation
        conf.setFloat("rec.social.regularization", description.getSocialReg());

        // Try to increase the cache size.
        conf.setStrings("guava.cache.spec", "maximumSize=2000,expireAfterAccess=20m");

        // Use a low learn rate.
        conf.setFloat("rec.iterator.learnrate.maximum", 0.1f);
        conf.setFloat("rec.iterator.learnrate", 0.0001f);
        conf.setBoolean("rec.recommender.earlystop", true);
    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        // build data model
        DataModel dataModel = new SharedDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SynchronousTrustSVDRecommender(this.randomSeed);
        recommender.recommend(context);

        return recommender;
    }
}

class SynchronousTrustSVDRecommender extends TrustSVDRecommender {

    private static Lock setupLock = new ReentrantLock();
    private ConvergenceTester convergenceTester;

    public SynchronousTrustSVDRecommender(long seed) {
        Randoms.seed(seed);
        this.convergenceTester = new ConvergenceTester(getClass().getSimpleName(), LOG);
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
