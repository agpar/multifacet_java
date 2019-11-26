package agpar.multifacet.single_features;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.User;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class GenerateAllSingle {
    public static void generateData(String path) {
        DataSet ds = DataSet.getInstance();
        ds.load(0, 1_000_000_000);
        HashMap<Integer, HashMap<String, Integer>> userFeats = computeUserFeatures(ds);
        writeUserFeatures(path, userFeats);
    }

    private static HashMap<Integer, HashMap<String, Integer>> computeUserFeatures(DataSet ds) {
        HashMap<Integer, HashMap<String, Integer>> usersToFeatures = new HashMap<>();

        for(User user : ds.getUsers()) {
            // Count outgoing trust links for users.
            HashMap<String, Integer> trusterFeats = usersToFeatures.getOrDefault(user.getUserIdInt(), new HashMap<>());
            trusterFeats.put("outgoingTrust", user.getFriendsInt().size());
            usersToFeatures.put(user.getUserIdInt(), trusterFeats);

            // Increment incoming trust links for all friends.
            for (Integer trusteeId : user.getFriendsInt()) {
                HashMap<String, Integer> trusteeFeats = usersToFeatures.getOrDefault(trusteeId, new HashMap<>());
                int currentIncoming = trusteeFeats.getOrDefault("incomingTrust", 0);
                trusteeFeats.put("incomingTrust", currentIncoming + 1);
                usersToFeatures.put(trusteeId, trusteeFeats);
            }
        }

        return usersToFeatures;
    }

    private static void writeUserFeatures(String path, HashMap<Integer, HashMap<String, Integer>> userFeatures) {

        try {
            Writer writer = new BufferedWriter(new FileWriter(path));

            // Write out a header.
            HashMap randomFeatureSet = userFeatures.values().iterator().next();
            String[] sortedKeys = (String[]) randomFeatureSet.keySet().toArray();
            Arrays.sort(sortedKeys);
            writer.write(String.join(",", sortedKeys) + "\n");

            for (Integer userId : userFeatures.keySet()) {
                String[] values = new String[sortedKeys.length];
                int i = 0;
                for (String key : sortedKeys) {
                    values[i] = userFeatures.get(userId).get(key).toString();
                    i++;
                }
                writer.write(String.join(",", values) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Could not open file to write single results!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
