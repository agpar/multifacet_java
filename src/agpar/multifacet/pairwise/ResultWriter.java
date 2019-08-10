package agpar.multifacet.pairwise;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import static java.lang.System.exit;

public class ResultWriter {
    public static void WriteResults(List<PairwiseResults> results) {
        BufferedWriter writer;
        try {
             writer = new BufferedWriter(
                    new FileWriter("/home/aparment/Documents/datasets/yelp/pcc.csv"));
            for (PairwiseResults result : results) {
                writer.write(String.format("%s\n", result.toString()));
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }

    }
}
