package agpar.multifacet.pairwise_features;

import java.util.*;

public class PairwiseResult {
    public int user1Id;
    public int user2Id;
    private HashMap<String, Double> results = new HashMap<>();
    private static Set<String> knownKeys = new HashSet<>(List.of(
            "PCC", "socialJacc", "areFriends", "areFriendsOfFriends", "itemJacc", "categoryJacc", "predictability"
    ));

    public PairwiseResult(int user1Id, int user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    public void addResult(String key, Double value) {
        if (!knownKeys.contains(key)) {
            throw new IllegalArgumentException("Unknown key " + key);
        }
        results.put(key, value);
    }
    public void addResult(String key, Boolean value) {
        if (!knownKeys.contains(key)) {
            throw new IllegalArgumentException("Unknown key " + key);
        }
        results.put(key, value ? 1.0 : 0.0);
    }

    public String toString() {
        ArrayList<String> sortedResults = new ArrayList<>();
        for (String key : sortedResultKeys()) {
            sortedResults.add(doubleFmt(results.get(key)));
        }

        return String.format("%d,%d,", user1Id, user2Id) + String.join(",", sortedResults);
    }

    private List<String> sortedResultKeys(){
        List<String> sortedKeys = new ArrayList<>(results.keySet());
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

    private static String doubleFmt(Double d) {
        final double threshold = 0.0000001;
        if (d == null) {
           return "null";
        }
        else if (Math.abs(d) < threshold) {
            return "0";
        } else if (Math.abs(d - 1.) < threshold) {
            return "1";
        }
        else {
            return String.format("%f", d);
        }
    }

    public static PairwiseResult fromString(String[] header, String commaSeparated) {
        String[] splitValues = commaSeparated.split(",");
        PairwiseResult res = new PairwiseResult(Integer.parseInt(splitValues[0]), Integer.parseInt(splitValues[1]));
        for(int i = 2; i < header.length; i++) {
            res.addResult(header[i], Double.parseDouble(splitValues[i]));
        }
        return res;
    }

    public String header() {
        return "user1Id,user2Id," + String.join(",", sortedResultKeys());
    }
}
