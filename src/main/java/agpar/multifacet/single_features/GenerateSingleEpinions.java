package agpar.multifacet.single_features;

import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.data_interface.data_classes.Review;
import agpar.multifacet.data_interface.data_classes.User;
import agpar.multifacet.data_interface.epinions.EpinionsUser;
import agpar.multifacet.pairwise_features.ReviewSimilarity;
import agpar.multifacet.pairwise_features.review_avg_calculators.GlobalReviewAvgCalculator;
import agpar.multifacet.pairwise_features.review_avg_calculators.ItemReviewAvgCalculator;
import agpar.multifacet.pairwise_features.review_avg_calculators.UserReviewAvgCalculator;

import java.io.*;
import java.util.HashMap;

public class GenerateSingleEpinions {

    private static GlobalReviewAvgCalculator globalAverageReviews;
    public static void generateData(String path) {
        DataSet ds = DataSet.getInstance();
        ds.load(0, 1_000_000_000);
        HashMap<Integer, HashMap<String, String>> userFeats = computeUserFeatures(ds);
        writeUserFeatures(path, userFeats);
    }

    private static HashMap<Integer, HashMap<String, String>> computeUserFeatures(DataSet ds) {
        HashMap<Integer, HashMap<String, String>> usersToFeatures = new HashMap<>();
        ItemReviewAvgCalculator avgCalculator = new ItemReviewAvgCalculator(ds.getReviewsByItemId());

        for(User user : ds.getUsers()) {
            // Count outgoing trust links for users.
            EpinionsUser epUser = (EpinionsUser) user;
            HashMap<String, String> trusterFeats = usersToFeatures.getOrDefault(user.getUserId(), new HashMap<>());
            trusterFeats.put("outgoingTrust", String.valueOf(epUser.getTrustLinksOutgoing().size()));
            trusterFeats.put("outgoingDistrust", String.valueOf(epUser.getDistrustLinksOutgoing().size()));
            trusterFeats.put("incomingTrust", String.valueOf(epUser.getTrustLinksIncoming().size()));
            trusterFeats.put("incomingDistrust", String.valueOf(epUser.getDistrustLinksIncoming().size()));
            trusterFeats.put("userId", String.valueOf(user.getUserId()));
            usersToFeatures.put(user.getUserId(), trusterFeats);

            trusterFeats.put("integrity", String.format("%f", (integrity(user, ds, avgCalculator))));
        }

        return usersToFeatures;
    }

    private static Double integrity(User u, DataSet ds, ItemReviewAvgCalculator avgCalculator){
        if (globalAverageReviews == null) {
            globalAverageReviews = new GlobalReviewAvgCalculator(ds.getReviewsByItemId());
        }

        Review[] u1Reviews = new Review[u.getReviews().size()];
        {
            int i = 0;
            for (Review review : u.getReviews()) {
                u1Reviews[i] = review;
                i++;
            }
        }
        double[] u1Avgs = new UserReviewAvgCalculator(ds.getReviewsByItemId()).getAvgs(u1Reviews, u);

        Review[] globalReviews = new Review[u.getReviews().size()];
        for(int i = 0; i < globalReviews.length; i++) {
            int itemReviewed = u1Reviews[i].getItemId();
            Review avgReview = new Review(
                    -1,
                    itemReviewed,
                    "",
                    avgCalculator.getItemAvg(itemReviewed)
            );
            globalReviews[i] = avgReview;
        }
        double[] globalAvgReviews = globalAverageReviews.getAvgs(globalReviews, null);

        return ReviewSimilarity.pcc(u1Reviews, u1Avgs, globalReviews, globalAvgReviews);
    };

    private static void writeUserFeatures(String path, HashMap<Integer, HashMap<String, String>> userFeatures) {

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
                HashMap<String, String> currentUserFeatures = userFeatures.get(userId);
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
