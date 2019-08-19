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


public class SoRecReommender extends RecRunner{

    @Override
    protected void learnImplementation() throws LibrecException {
        // recommender configuration
//        conf.setFloat("rec.social.regularization", 1f);
//        conf.setFloat("rec.rate.social.regularization", 0.01F);
//        conf.setFloat("rec.user.social.regularization", 0.01F);

        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SoRecRecommender();
        recommender.recommend(context);

        // evaluation
        RecommenderEvaluator evaluator = new MAEEvaluator();
        Double MAE = recommender.evaluate(evaluator);
        RecommenderEvaluator evaluator2 = new MSEEvaluator();
        Double MSE = recommender.evaluate(evaluator2);
        System.out.printf("MAE: %f\n", MAE);
        System.out.printf("MSE: %f\n", MSE);
    }
}
