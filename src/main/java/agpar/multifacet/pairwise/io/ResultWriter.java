package agpar.multifacet.pairwise.io;

import agpar.multifacet.pairwise.PairwiseResult;

import java.io.IOException;
import java.util.List;

public interface ResultWriter {
    void open() throws IOException;
    void close() throws IOException;
    void writeResults(List<PairwiseResult> results) throws IOException;
    void writeResults(String result) throws IOException;
}
