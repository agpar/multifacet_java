package agpar.multifacet.recommend;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.DataConvertor;
import net.librec.data.DataSplitter;
import net.librec.data.splitter.KCVDataSplitter;
import net.librec.math.structure.SparseMatrix;

class StaticDataHolder {
    private static KCVDataSplitter splitter;
    private static SparseMatrix[] trainMatrices;
    private static SparseMatrix[] testMatrices;
    private static int size;

    public StaticDataHolder(int kFolds, DataConvertor dataConvertor, Configuration conf) {
        splitter = new KCVDataSplitter(dataConvertor, conf);
        trainMatrices = new SparseMatrix[kFolds];
        testMatrices = new SparseMatrix[kFolds];
        size = kFolds;
        splitter.splitFolds(kFolds);
    }

    public synchronized SparseMatrix getTest(int i) throws LibrecException{
        if (i < 0 || i >= size) {
            return null;
        }
        if (testMatrices[i] == null) {
            initFold(i);
        }
        return testMatrices[i];
    }

    public synchronized SparseMatrix getTrain(int i) throws LibrecException{
        if (i < 0 || i >= size) {
            return null;
        }
        if (trainMatrices[i] == null) {
            System.out.println("Generating fold matrix " + i);
            initFold(i);
        }
        else {
            System.out.println("Re using fold matrix " + i);
        }
        return trainMatrices[i];
    }

    private synchronized void initFold(int i) throws LibrecException{
        splitter.splitData(i);
        trainMatrices[i] = new SparseMatrix(splitter.getTrainData());
        testMatrices[i] = new SparseMatrix(splitter.getTestData());
    }

    public int getSize() {
        return size;
    }
}

public class ThreadSafeKCVDataSplitter implements DataSplitter {
    private int kIndex;
    private int kFolds;

    private static StaticDataHolder holder;

    private ThreadSafeKCVDataSplitter(int kIndex, int kFolds) {
        this.kIndex = kIndex;
        this.kFolds = kFolds;
    }

    public synchronized static ThreadSafeKCVDataSplitter getInstance(int kIndex, int kFolds, DataConvertor dataConverter, Configuration conf) {
        if (holder == null || holder.getSize() != kFolds) {
            holder = new StaticDataHolder(kFolds, dataConverter, conf);
        }
        return new ThreadSafeKCVDataSplitter(kIndex, kFolds);
    }

    public synchronized void reset() {
        holder = null;
    }

    @Override
    public void splitData() throws LibrecException {
        throw new LibrecException();
    }

    @Override
    public void setDataConvertor(DataConvertor dataConvertor) {
        return;
    }

    @Override
    public SparseMatrix getTrainData() {
        SparseMatrix train = null;
        try {
            train = holder.getTrain(kIndex);
        } catch (LibrecException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return train;
    }

    @Override
    public SparseMatrix getTestData() {
        SparseMatrix train = null;
        try {
            train = holder.getTest(kIndex);
        } catch (LibrecException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return train;
    }

    @Override
    public SparseMatrix getValidData() {
        return null;
    }
}
