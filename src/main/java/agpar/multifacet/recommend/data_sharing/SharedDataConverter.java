package agpar.multifacet.recommend.data_sharing;

import com.google.common.collect.BiMap;
import net.librec.data.convertor.AbstractDataConvertor;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.math.structure.SparseMatrix;

import java.io.IOException;
import java.util.HashMap;

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
