package agpar.multifacet.pairwise_features.io;

import agpar.multifacet.pairwise_features.PairwiseResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;


/*
After writing this thing I realized it was probably  unnecessary, as a file opened in append  mode should
have atomic writes ANYWAY. Oh well.
 */
public class SynchronizedAppendResultWriter implements ResultWriter{
    private String filePath;
    private BufferedWriter writer;
    private static HashMap<String, SynchronizedAppendResultWriter> staticWriters = new HashMap<>();

    public SynchronizedAppendResultWriter(String filePath) {
        this.filePath = filePath;
    }

    public synchronized void writeResults(List<PairwiseResult> results) throws IOException{
        if (this.writer == null) {
            this.open();
        }

        for (PairwiseResult result : results) {
            this.writer.write(String.format("%s\n", result.toString()));
        }
    }

    public synchronized void writeResults(String result) throws IOException {
        if (this.writer == null) {
            this.openNoHeader();
        }
        this.writer.write(result);
    }

    private synchronized void openNoHeader() throws IOException {
        this.writer = new BufferedWriter(new FileWriter(this.filePath, true));
    }

    public synchronized void open() throws IOException{
        boolean alreadyExisted = Files.exists(Path.of(this.filePath));
        this.writer = new BufferedWriter(new FileWriter(this.filePath, true));
        if (! alreadyExisted) {
            this.writer.write(String.format("%s\n", PairwiseResult.header()));
        }
    }

    public synchronized void close() throws IOException{
        if (this.writer != null) {
            this.writer.close();
        }
    }

    public synchronized void flush() throws IOException {
        if (this.writer != null)
            this.writer.flush();
    }

    public static void flushAll() throws IOException{
        for (SynchronizedAppendResultWriter staticWriter: staticWriters.values()) {
            staticWriter.flush();
        }
    }

    public static synchronized SynchronizedAppendResultWriter getSingleton(String path) {
        if (SynchronizedAppendResultWriter.staticWriters.containsKey(path)) {
            return SynchronizedAppendResultWriter.staticWriters.get(path);
        } else {
            SynchronizedAppendResultWriter writer = new SynchronizedAppendResultWriter(path);
            SynchronizedAppendResultWriter.staticWriters.put(path, writer);
            return writer;
        }
    }

    public static synchronized void closeAllSingletons() throws IOException {
        for (ResultWriter writer : SynchronizedAppendResultWriter.staticWriters.values()) {
            writer.close();
        }
    }
}