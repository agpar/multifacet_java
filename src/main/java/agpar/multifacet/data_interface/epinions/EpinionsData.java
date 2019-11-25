package agpar.multifacet.data_interface.epinions;

import agpar.multifacet.Settings;
import agpar.multifacet.data_interface.DataSet;

public class EpinionsData extends DataSet {
    private EpinionsDataReader reader;

    public EpinionsData() {
        this.reader = new EpinionsDataReader(Settings.EPINIONS_DATA_DIR());
    }

    @Override
    public void load(int start, int stop) {
        System.out.println("Loading Users");
        this.usersById = reader.loadUsers();
        System.out.println("Loading Reviews");
        this.reviewsByItemId = reader.loadTrainReviews();
        System.out.println("Loading Businesses");
        this.businesses = reader.loadBusinesses();
    }
}
