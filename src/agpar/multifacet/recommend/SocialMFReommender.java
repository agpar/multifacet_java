package agpar.multifacet.recommend;

import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.MSEEvaluator;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.SocialMFRecommender;


public class SocialMFReommender {
    public static void learn(String exp_dir, String rating_file, String social_file) throws net.librec.common.LibrecException {
        // recommender configuration
        Configuration conf = new Configuration();
        conf.set("data.input.path", rating_file);
        conf.set("dfs.data.dir", exp_dir);
        conf.set("data.appender.path", social_file);
        conf.set("data.appender.class", "net.librec.data.convertor.appender.SocialDataAppender");
        conf.set("rec.neighbors.knn.number", "3");
        conf.set("rec.iterator.maximum", "100");

        Randoms.seed(1);

        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new SocialMFRecommender();
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
