package agpar.multifacet.recommend;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.MSEEvaluator;
import net.librec.recommender.Recommender;

import java.util.Dictionary;
import java.util.HashMap;

public abstract class RecRunner {
    protected String experimentDir;
    protected String ratingFile;
    protected String socialFile;
    public Configuration conf;

    public  HashMap<String, Double> learn(String experimentDir, String ratingFile, String socialFile) throws LibrecException {
        this.experimentDir = experimentDir;
        this.ratingFile = ratingFile;
        this.socialFile = socialFile;
        this.conf = new Configuration();
        conf.set("data.input.path", this.ratingFile);
        conf.set("dfs.data.dir", this.experimentDir);
        conf.set("data.appender.path", this.socialFile);
        conf.set("data.appender.class", "net.librec.data.convertor.appender.SocialDataAppender");
        conf.set("rec.iterator.maximum", "1000");
        Recommender recommender = this.learnImplementation();
        return this.evaluate(recommender);
    };

    protected HashMap<String, Double> evaluate(Recommender recommender) throws LibrecException{
        RecommenderEvaluator evaluator = new MAEEvaluator();
        Double MAE = recommender.evaluate(evaluator);
        RecommenderEvaluator evaluator2 = new MSEEvaluator();
        Double MSE = recommender.evaluate(evaluator2);

        HashMap<String, Double> results = new HashMap<>();
        results.put("MAE", MAE);
        results.put("MSE", MSE);
        return results;

    }
    protected abstract Recommender learnImplementation() throws LibrecException;
}
