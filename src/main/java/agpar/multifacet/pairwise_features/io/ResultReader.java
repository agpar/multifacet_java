package agpar.multifacet.pairwise_features.io;

import agpar.multifacet.pairwise_features.PairwiseResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ResultReader {
    private String filePath;
    private BufferedReader reader;

    public ResultReader(String filePath) {
        this.filePath = filePath;
    }

    public List<PairwiseResult> read() throws IOException{
        ArrayList<PairwiseResult> results = new ArrayList<>();
        if (this.reader == null) {
            this.open();
        }

        // Throw away the header.
        String line = this.reader.readLine();
        line = this.reader.readLine();
        int lineno =1;
        while(line != null) {
            try {
                results.add(PairwiseResult.fromString(line));
            } catch (Exception e) {
                e.printStackTrace();
            }
            line = this.reader.readLine();
            lineno++;
        }
        return results;
    }

    public void open() throws IOException{
        this.reader = new BufferedReader(new FileReader(this.filePath));
    }

    public void close() throws IOException{
        this.reader.close();
    }
}
