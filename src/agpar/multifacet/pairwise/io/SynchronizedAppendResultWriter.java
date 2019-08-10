package agpar.multifacet.pairwise.io;

import agpar.multifacet.pairwise.PairwiseResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class SynchronizedAppendResultWriter implements ResultWriter{
    private String filePath;
    private Writer writer;

    public SynchronizedAppendResultWriter(String filePath) {
        this.filePath = filePath;
    }

    public synchronized void WriteResults(List<PairwiseResult> results) throws IOException{
        if (this.writer == null) {
            this.open();
        }

        for (PairwiseResult result : results) {
            this.writer.write(String.format("%s\n", result.toString()));
        }
    }

    public synchronized void open() throws IOException{
        boolean alreadyExisted = Files.exists(Path.of(this.filePath));
        this.writer = new BufferedWriter(new FileWriter(this.filePath, true));
        if (! alreadyExisted) {
            this.writer.write(String.format("%s\n", PairwiseResult.header()));
        }
    }

    public synchronized void close() throws IOException{
        this.writer.close();
    }
}
