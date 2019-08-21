package agpar.multifacet.recommend;

import agpar.multifacet.experiments.ExperimentDescription;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.context.rating.TrustSVDRecommender;

public class TrustSVDTester extends RecommenderTester {
    @Override
    public void loadDescription(ExperimentDescription description) {
        super.loadDescription(description);
        conf.setFloat("rec.social.regularization", description.getSocialReg());
    }

    @Override
    protected Recommender learnImplementation() throws LibrecException {
        // build data model
        DataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // set recommendation context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // training
        Recommender recommender = new TrustSVDRecommender();
        recommender.recommend(context);

        return recommender;
    }
}
