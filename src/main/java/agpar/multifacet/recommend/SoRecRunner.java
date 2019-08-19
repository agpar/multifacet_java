package agpar.multifacet.recommend;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.MSEEvaluator;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.SoRecRecommender;


public class SoRecRunner extends RecRunner{

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        /*
        The important hyper parameter here is rec.social.regularization, as this controls the level
        of impact the social graph has on the optimization.
        Social graph should have entries in (0, 1], where a 0 indicates an unknown.

        I'm pretty sure 'rec.social.regularization' is not actually used by this recommender.
         */

        //conf.setFloat("rec.social.regularization", 10F);
        conf.setFloat("rec.rate.social.regularization", 10F);
        conf.setFloat("rec.user.social.regularization", 0.01F);

        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SoRecRecommender();
        recommender.recommend(context);

        return recommender;
    }
}
