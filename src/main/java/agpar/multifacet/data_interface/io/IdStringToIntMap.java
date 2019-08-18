package agpar.multifacet.data_interface.io;

import java.util.HashMap;

public class IdStringToIntMap {
    private HashMap<String, Integer> stringIntIdMap = new HashMap<>();
    private int nextInt = 0;

    public synchronized int getInt(String id) {
        if (this.stringIntIdMap.containsKey(id)) {
            return this.stringIntIdMap.get(id);
        }
        int hashedId = nextInt;
        nextInt++;
        this.stringIntIdMap.put(id, hashedId);
        return hashedId;
    }
}
