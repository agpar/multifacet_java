package agpar.multifacet;

import agpar.multifacet.experiments.GenerateAllPairwise;
import agpar.multifacet.experiments.GenerateFriendsOnly;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        String path = Path.of(Settings.RAM_DATA_DIR, "all_500kfriends_3overlap_itemavg2.csv").toString();
        GenerateFriendsOnly.generateData(path, 500_000);

    }
}
