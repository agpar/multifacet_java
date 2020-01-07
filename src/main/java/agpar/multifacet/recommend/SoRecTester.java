package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.SoRecRecommender;


public class SoRecTester extends RecommenderTester {

    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);
        conf.setFloat("rec.rate.social.regularization", description.getSocialReg());

    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        /*
        The important hyper parameter here is rec.rate.social.regularization as this controls the level
        of impact the social graph has on the optimization.
        Social graph should have entries in (0, 1], where a 0 indicates an unknown.

        I'm pretty sure 'rec.social.regularization' is not actually used by this recommender.
         */

        // build data model
        DataModel dataModel = new SharedDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SoRecRecommender();
        recommender.recommend(context);

        return recommender;
    }
}
