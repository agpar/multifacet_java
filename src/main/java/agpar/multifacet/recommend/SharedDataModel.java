package agpar.multifacet.recommend;

import com.google.common.collect.BiMap;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.conf.Configured;
import net.librec.data.DataConvertor;
import net.librec.data.DataModel;
import net.librec.data.convertor.AbstractDataConvertor;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.data.model.AbstractDataModel;
import net.librec.math.structure.DataSet;
import net.librec.math.structure.SparseMatrix;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;

public class SharedDataModel extends AbstractDataModel implements DataModel {

    static DataConvertor convertor;

    public SharedDataModel() {
    }

    public SharedDataModel(Configuration conf) {
        this.conf = conf;
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

    @Override
    public DataSet getDatetimeDataSet() {
        return null;
    }

    @Override
    public BiMap<String, Integer> getUserMappingData() {
        return null;
    }

    @Override
    public BiMap<String, Integer> getItemMappingData() {
        return null;
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
}
