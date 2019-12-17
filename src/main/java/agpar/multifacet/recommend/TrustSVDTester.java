package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.TrustSVDRecommender;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TrustSVDTester extends RecommenderTester {
    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);
        conf.setFloat("rec.social.regularization", description.getSocialReg());
        // Try to increase the cache size.
        conf.setStrings("guava.cache.spec", "maximumSize=2000,expireAfterAccess=20m");
    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        // build data model
        DataModel dataModel = new SharedDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SynchronousTrustSVDRecommender();
        recommender.recommend(context);

        return recommender;
    }
}
// Gets around a race condition in the setup function.
class SynchronousTrustSVDRecommender extends TrustSVDRecommender {

    private static Lock setupLock = new ReentrantLock();

    public void setup() throws LibrecException
    {
        setupLock.lock();
        super.setup();
        setupLock.unlock();
    }
}
