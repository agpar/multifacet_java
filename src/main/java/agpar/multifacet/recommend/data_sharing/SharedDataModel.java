package agpar.multifacet.recommend.data_sharing;

import com.google.common.collect.BiMap;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataConvertor;
import net.librec.data.DataModel;
import net.librec.data.model.AbstractDataModel;
import net.librec.data.splitter.GivenTestSetDataSplitter;
import net.librec.math.structure.DataSet;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

public class SharedDataModel extends AbstractDataModel implements DataModel {

    static DataConvertor convertor;

    public SharedDataModel() {
    }

    public SharedDataModel(Configuration conf) {
        this.conf = conf;
    }

    public static synchronized void resetSocial() {
        SharedSocialDataAppender.reset();
    }

    @Override
    protected void buildConvert() throws LibrecException {
        String[] inputDataPath = conf.get(Configured.CONF_DATA_INPUT_PATH).trim().split(" ");
        for(int i = 0 ; i < inputDataPath.length; i ++){
            inputDataPath[i]=conf.get(Configured.CONF_DFS_DATA_DIR)+"/"+inputDataPath[i];
        }
        String dataColumnFormat = conf.get(Configured.CONF_DATA_COLUMN_FORMAT, "UIR");
        dataConvertor = SharedDataConverter.getInstance(dataColumnFormat, StringUtils.join(inputDataPath," "), conf.getDouble("data.convert.binarize.threshold", -1.0));
        try {
            dataConvertor.processData();
        } catch (IOException e) {
            throw new LibrecException(e);
        }
    }

    protected void buildSplitter() throws LibrecException {
        int splitIndex = conf.getInt("data.splitter.cv.index", 1);
        conf.setInt("data.splitter.cv.index", splitIndex);
        int splitNum = conf.getInt("data.splitter.cv.number", 5);
        conf.setInt("data.splitter.cv.number", splitNum);

        //dataSplitter = SharedRatioDataSplitter.getInstance(dataConvertor, conf);
        //dataSplitter = ThreadSafeKCVDataSplitter.getInstance(splitIndex, splitNum,  dataConvertor, conf);
        dataSplitter = new GivenTestSetDataSplitter(dataConvertor, conf);

        dataSplitter.splitData();
        trainDataSet = dataSplitter.getTrainData();
        testDataSet = dataSplitter.getTestData();
    }

    protected void buildFeature() throws LibrecException {
        String feature = conf.get("data.appender.class");
        if (StringUtils.isNotBlank(feature)) {
            try {
                dataAppender = SharedSocialDataAppender.getInstance(conf);
                dataAppender.setUserMappingData(getUserMappingData());
                dataAppender.setItemMappingData(getItemMappingData());
                dataAppender.processData();
            } catch (IOException e) {
                throw new LibrecException(e);
            }
        }
    }

    @Override
    public BiMap<String, Integer> getUserMappingData() {
        return ((SharedDataConverter) dataConvertor).getUserIds();
    }

    @Override
    public BiMap<String, Integer> getItemMappingData() {
        return ((SharedDataConverter) dataConvertor).getItemIds();
    }

   @Override
    public DataSet getDatetimeDataSet() {
        return ((SharedDataConverter) dataConvertor).getDatetimeMatrix();
    }
}




