package agpar.multifacet.recommend.data_sharing;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.DataConvertor;
import net.librec.data.DataSplitter;
import net.librec.data.splitter.RatioDataSplitter;
import net.librec.math.structure.SparseMatrix;

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
