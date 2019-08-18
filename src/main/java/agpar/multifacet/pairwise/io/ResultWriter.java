package agpar.multifacet.pairwise.io;

import agpar.multifacet.pairwise.PairwiseResult;

import java.io.IOException;
import java.util.List;

public interface ResultWriter {
    void open() throws IOException;
    void close() throws IOException;
    void WriteResults(List<PairwiseResult> results) throws IOException;
}
