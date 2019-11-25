package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;

public class EpinionsData extends DataSet {
    private DataReader reader;

    public EpinionsData() {
        this.reader = new DataReader(Settings.EPINIONS_DATA_DIR());
    }

    @Override
    public void load(int start, int stop) {

    }
}
