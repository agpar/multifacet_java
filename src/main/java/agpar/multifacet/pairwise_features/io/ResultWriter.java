package agpar.multifacet.pairwise_features.io;

import agpar.multifacet.pairwise_features.PairwiseResult;

import java.io.IOException;
import java.util.List;

public interface ResultWriter {
    void open() throws IOException;
    void close() throws IOException;
    void writeResults(List<PairwiseResult> results) throws IOException;
    void writeResults(String result) throws IOException;
}
