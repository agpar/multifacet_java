package agpar.multifacet.single_features;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.epinions.EpinionsUser;

import java.io.*;
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
            EpinionsUser epUser = (EpinionsUser) user;
            HashMap<String, Integer> trusterFeats = usersToFeatures.getOrDefault(user.getUserIdInt(), new HashMap<>());
            trusterFeats.put("outgoingTrust", user.getFriendsLinksOutgoing().size());
            trusterFeats.put("outgoingDistrust", epUser.getDistrustedUsers().size());
            trusterFeats.put("incomingTrust", 0);
            trusterFeats.put("incomingDistrust", 0);
            trusterFeats.put("userId", user.getUserIdInt());
            usersToFeatures.put(user.getUserIdInt(), trusterFeats);

            // Increment incoming trust links for all friends.
            for (Integer trusteeId : user.getFriendsLinksOutgoing()) {
                HashMap<String, Integer> trusteeFeats = usersToFeatures.getOrDefault(trusteeId, new HashMap<>());
                int currentIncoming = trusteeFeats.getOrDefault("incomingTrust", 0);
                trusteeFeats.put("incomingTrust", currentIncoming + 1);
                usersToFeatures.put(trusteeId, trusteeFeats);
            }

            // Increment incoming distrust links for all enemies
            for (Integer trusteeId : epUser.getDistrustedUsers()) {
                HashMap<String, Integer> trusteeFeats = usersToFeatures.getOrDefault(trusteeId, new HashMap<>());
                int currentIncoming = trusteeFeats.getOrDefault("incomingDistrust", 0);
                trusteeFeats.put("incomingDistrust", currentIncoming + 1);
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
            String[] sortedKeys = new String[randomFeatureSet.keySet().size()] ;
            sortedKeys[0] = "userId";
            int i = 1;
            for(Object key : randomFeatureSet.keySet()) {
                if (key.equals("userId")) {
                    continue;
                }
                sortedKeys[i] = (String) key;
                i++;
            }
            writer.write(String.join(",", sortedKeys) + "\n");

            for (Integer userId : userFeatures.keySet()) {
                HashMap<String, Integer> currentUserFeatures = userFeatures.get(userId);
                // Bail if user is not included in data set (is only referenced by other users)
                if (! currentUserFeatures.containsKey("userId")) {
                    continue;
                }
                String[] values = new String[sortedKeys.length];
                i= 0;
                for (String key : sortedKeys) {
                    values[i] = userFeatures.get(userId).get(key).toString();
                    i++;
                }
                writer.write(String.join(",", values) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not open file to write single results!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
