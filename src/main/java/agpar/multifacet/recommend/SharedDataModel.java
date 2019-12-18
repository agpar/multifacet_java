package agpar.multifacet.recommend;

import com.google.common.collect.BiMap;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataAppender;
import net.librec.data.DataConvertor;
import net.librec.data.DataModel;
import net.librec.data.DataSplitter;
import net.librec.data.convertor.AbstractDataConvertor;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.data.convertor.appender.SocialDataAppender;
import net.librec.data.model.AbstractDataModel;
import net.librec.data.splitter.RatioDataSplitter;
import net.librec.math.structure.DataSet;
import net.librec.math.structure.SparseMatrix;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;

/*
A bunch of decorators around internal librec classes which allow  multiple threads running
experiments which should operate on the same set of data to share a single set of in memory data.
 */
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

        dataSplitter = ThreadSafeKCVDataSplitter.getInstance(splitIndex, splitNum,  dataConvertor, conf);
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

class SharedDataConverter extends AbstractDataConvertor {

    private static HashMap<String, SharedDataConverter> instances = new HashMap<>();
    private TextDataConvertor tdc;

    private SharedDataConverter(String dataColumnFormat, String inputPath, double binThold) {
        tdc = new TextDataConvertor(dataColumnFormat, inputPath, binThold);
    }

    public static synchronized SharedDataConverter getInstance(String dataColumnFormat, String inputPath, double binThold) {
        String key = String.format("%s%s%f", dataColumnFormat, inputPath, binThold);
        if (!instances.containsKey(key)) {
            instances.put(key, new SharedDataConverter(dataColumnFormat, inputPath, binThold));
        }
        return instances.get(key);
    }

    public static synchronized void reset() {
        instances.clear();
    }

    public synchronized void processData() throws IOException {
        if (tdc.getPreferenceMatrix() == null) {
            tdc.processData();
        }
    }

    public SparseMatrix getPreferenceMatrix() {
        return tdc.getPreferenceMatrix();
    }

    @Override
    public void progress() {
        tdc.progress();
    }

    public BiMap<String, Integer> getUserIds() {
        return tdc.getUserIds();
    }

    public BiMap<String, Integer> getItemIds() {
        return tdc.getItemIds();
    }
}

class SharedSocialDataAppender extends SocialDataAppender implements DataAppender {
    private static HashMap<String, SharedSocialDataAppender> instances = new HashMap<>();
    private SocialDataAppender sda;

    private SharedSocialDataAppender(Configuration conf) {
        this.sda = new SocialDataAppender(conf);
    }

    public static synchronized SharedSocialDataAppender getInstance(Configuration conf) {
        String key = conf.get("data.appender.path");
        if(!instances.containsKey(key)) {
            instances.put(key, new SharedSocialDataAppender(conf));
        }
        return instances.get(key);
    }

    public static synchronized void reset() {
        instances.clear();
    }

    @Override
    public synchronized void processData() throws IOException {
        if (sda.getUserAppender() == null) {
            sda.processData();
        }
    }

    public SparseMatrix getUserAppender() {
        return sda.getUserAppender();
    }

    @Override
    public void setUserMappingData(BiMap<String, Integer> userMappingData) {
        sda.setUserMappingData(userMappingData);

    }

    @Override
    public void setItemMappingData(BiMap<String, Integer> itemMappingData) {
        sda.setItemMappingData(itemMappingData);
    }
}

class SharedRatioDataSplitter implements DataSplitter {

    private static DataSplitter rdt;
    private static SharedRatioDataSplitter instance;

    private SharedRatioDataSplitter(DataConvertor dataConvertor, Configuration conf) {
        rdt = new RatioDataSplitter(dataConvertor, conf);
    }

    public synchronized static SharedRatioDataSplitter getInstance(DataConvertor dataConvertor, Configuration conf) {
        if (instance == null) {
            instance = new SharedRatioDataSplitter(dataConvertor, conf);
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    @Override
    public synchronized void splitData() throws LibrecException {
        if (rdt.getTrainData() == null) {
            rdt.splitData();
        }
    }

    @Override
    public void setDataConvertor(DataConvertor dataConvertor) {
        rdt.setDataConvertor(dataConvertor);
    }

    @Override
    public SparseMatrix getTrainData() {
        return rdt.getTrainData();
    }

    @Override
    public SparseMatrix getTestData() {
        return rdt.getTestData();
    }

    @Override
    public SparseMatrix getValidData() {
        return rdt.getValidData();
    }
}

