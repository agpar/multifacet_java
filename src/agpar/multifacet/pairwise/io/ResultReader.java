package agpar.multifacet.pairwise.io;

import agpar.multifacet.pairwise.PairwiseResult;

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
        while(line != null) {
            results.add(PairwiseResult.fromString(line));
            line = this.reader.readLine();
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
