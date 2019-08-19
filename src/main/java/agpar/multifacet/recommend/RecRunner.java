package agpar.multifacet.recommend;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;

public abstract class RecRunner {
    protected String experimentDir;
    protected String ratingFile;
    protected String socialFile;
    public Configuration conf;

    public  void learn(String experimentDir, String ratingFile, String socialFile) throws LibrecException {
        this.experimentDir = experimentDir;
        this.ratingFile = ratingFile;
        this.socialFile = socialFile;
        this.conf = new Configuration();
        conf.set("data.input.path", this.ratingFile);
        conf.set("dfs.data.dir", this.experimentDir);
        conf.set("data.appender.path", this.socialFile);
        conf.set("data.appender.class", "net.librec.data.convertor.appender.SocialDataAppender");
        conf.set("rec.iterator.maximum", "1000");
        this.learnImplementation();
    };

    protected abstract void learnImplementation() throws LibrecException;
}
