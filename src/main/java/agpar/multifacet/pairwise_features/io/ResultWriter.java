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
public class ResultWriter {
    private String filePath;
    private BufferedWriter writer;
    private boolean needsHeader = false;
    private static HashMap<String, ResultWriter> staticWriters = new HashMap<>();

    public ResultWriter(String filePath) {
        this.filePath = filePath;
    }

    public synchronized void writeResults(List<PairwiseResult> results) throws IOException{
        if (results.size() == 0) {
            return;
        }

        if (this.writer == null) {
            this.open();
        }

        if (needsHeader) {
            this.writer.write(String.format("%s\n", results.get(0).header()));
            needsHeader = false;
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
        if (! alreadyExisted) {
            needsHeader = true;
        }
        this.writer = new BufferedWriter(new FileWriter(this.filePath, true));
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
        for (ResultWriter staticWriter: staticWriters.values()) {
            staticWriter.flush();
        }
    }

    public static synchronized ResultWriter getSingleton(String path) {
        if (ResultWriter.staticWriters.containsKey(path)) {
            return ResultWriter.staticWriters.get(path);
        } else {
            ResultWriter writer = new ResultWriter(path);
            ResultWriter.staticWriters.put(path, writer);
            return writer;
        }
    }

    public static synchronized void closeAllSingletons() throws IOException {
        for (ResultWriter writer : ResultWriter.staticWriters.values()) {
            writer.close();
        }
    }
}
