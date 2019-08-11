package agpar.multifacet.data_interface.io;

import java.util.HashMap;
import java.util.HashSet;

public class IdStringToIntMap {
    private HashMap<String, Integer> stringIntIdMap = new HashMap<>();
    private HashSet<Integer> usedInts = new HashSet<>();

    public synchronized int getInt(String id) {
        if (this.stringIntIdMap.containsKey(id)) {
            return this.stringIntIdMap.get(id);
        }
        int hashedId = id.hashCode();
        while(usedInts.contains(hashedId)) {
            hashedId++;
        }
        this.stringIntIdMap.put(id, hashedId);
        this.usedInts.add(hashedId);
        return hashedId;
    }
}
