package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.TrustMFRecommender;

public class TrustMFTester extends RecommenderTester {

    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);
        conf.setFloat("rec.social.regularization", description.getSocialReg());
        //conf.setBoolean("rec.recommender.isranking", true);
        //conf.setInt("rec.recommender.ranking.topn", 10);
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
        Recommender recommender = new TrustMFRecommender();
        recommender.recommend(context);

        return recommender;
    }
}
