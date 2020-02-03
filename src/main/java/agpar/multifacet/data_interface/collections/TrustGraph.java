package agpar.multifacet.data_interface.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TrustGraph {
    private HashMap<Integer, HashSet<Integer>> outgoing = new HashMap<>();
    private HashMap<Integer, HashSet<Integer>> incoming = new HashMap<>();

    private static TrustGraph trustGlobal;
    private static TrustGraph distrustGlobal;

    public void addDirectedLink(int source, int dest) {
        getSet(outgoing, source).add(dest);
        getSet(incoming, dest).add(source);
    }

    public void addMutualLink(int source, int dest) {
        addDirectedLink(source, dest);
        addDirectedLink(dest, source);
    }

    public Set<Integer> getOutgoing(int source) {
        return Collections.unmodifiableSet(getSet(outgoing, source));
    }

    public Set<Integer> getIncoming(int dest) {
        return Collections.unmodifiableSet(getSet(incoming, dest));
    }

    private HashSet<Integer> getSet(HashMap<Integer, HashSet<Integer>> map, int key) {
        return map.computeIfAbsent(key, x -> new HashSet<Integer>());
    }

    public static TrustGraph getTrustGlobal() {
        if (trustGlobal == null)
            trustGlobal = new TrustGraph();
        return trustGlobal;
    }

    public static TrustGraph getDistrustGlobal() {
        if (distrustGlobal == null)
            distrustGlobal = new TrustGraph();
        return distrustGlobal;
    }

    public static void resetGlobals() {
        trustGlobal = null;
        distrustGlobal = null;
    }
}
