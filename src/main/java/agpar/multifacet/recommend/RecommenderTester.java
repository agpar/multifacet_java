package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.eval.rating.MAEEvaluator;
import net.librec.eval.rating.MSEEvaluator;
import net.librec.eval.rating.RMSEEvaluator;
import net.librec.math.algorithm.Randoms;
import net.librec.recommender.Recommender;

import java.nio.file.Path;
import java.util.HashMap;

public abstract class RecommenderTester {
    protected String baseExperimentDir;
    protected String experimentName;
    protected String experimentDir;
    protected String ratingTrainFile;
    protected String ratingTestFile;
    protected String socialFile;
    protected long randomSeed;
    public Configuration conf = new Configuration();

    public  HashMap<String, Double> learn(String experimentDir, String experimentName, String ratingTrainFile, String ratingTestFile, String socialFile) throws LibrecException {
        this.baseExperimentDir = experimentDir;
        this.experimentName = experimentName;
        this.experimentDir = Path.of(experimentDir, experimentName).toString();
        this.ratingTrainFile = ratingTrainFile;
        this.ratingTestFile = ratingTestFile;
        this.socialFile = socialFile;

        conf.set("dfs.data.dir", this.baseExperimentDir);
        conf.set("data.input.path", this.ratingTrainFile + " " + this.ratingTestFile);
        conf.set("data.testset.path", this.ratingTestFile);
        conf.set("data.model.splitter", "net.librec.data.splitter.GivenTestSetDataSplitter");
        conf.set("data.appender.path", this.socialFile);
        conf.set("data.appender.class", "net.librec.data.convertor.appender.SocialDataAppender");

        Recommender recommender = this.learnImplementation();
        return this.evaluate(recommender);
    };

    protected HashMap<String, Double> evaluate(Recommender recommender) throws LibrecException{
        HashMap<String, Double> results = new HashMap<>();
        results.put("MAE", recommender.evaluate(new MAEEvaluator()));
        results.put("RMSE", recommender.evaluate(new RMSEEvaluator()));
        results.put("MSE", recommender.evaluate(new MSEEvaluator()));

        return results;
    }

    public void loadDescription(ExperimentDescription description) {
        conf.set("rec.iterator.maximum", String.valueOf(description.getNumIterations()));
        conf.setInt("rec.factor.number", description.getLatentDim());
        conf.setLong("rec.random.seed", description.getRandomSeed());
        this.randomSeed = description.getRandomSeed();
        Randoms.seed(description.getRandomSeed());
    };

    protected abstract Recommender learnImplementation() throws LibrecException;
}
