package agpar.multifacet;

import agpar.multifacet.experiments.GenerateAllPairwise;
import agpar.multifacet.experiments.GenerateFriendsOnly;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        String path = Path.of(Settings.RAM_DATA_DIR, "all_100k_3overlap_itemavg.csv").toString();
        GenerateAllPairwise.generateData(path, 100_000);

    }
}
