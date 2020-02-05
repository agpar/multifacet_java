package agpar.multifacet.stats;

import agpar.multifacet.data_interface.DataSet;

import java.util.*;

public class DataStatsCalculator {
    public static class StatsBundle {
        public double mean;
        public double median;
        public double mode;
        public double min;
        public double max;

        public StatsBundle(double[] items) {
            mean = DataStatsCalculator.mean(items);
            median = DataStatsCalculator.median(items);
            mode = DataStatsCalculator.mode(items);
            min = DataStatsCalculator.min(items);
            max = DataStatsCalculator.max(items);
        }

        public String toString() {
            String fstring = "mean: %f, median %f, mode %f, min %f, max %f";
            return String.format(fstring, mean, median, mode, min, max);
        }
    }

    public static StatsBundle userReviewCount(DataSet ds) {
       var users = ds.getUsers();
       double[] reviewCounts = new double[users.size()];
       int i = 0;
       for (var user : users) {
           reviewCounts[i] = user.getReviews().size();
           i++;
       }

       return new StatsBundle(reviewCounts);
    }

    public static StatsBundle itemReviewCount(DataSet ds) {
        var reviews = ds.getReviewsByItemId();
        var itemCount = reviews.keys().size();
        double[] itemReviewCounts = new double[itemCount];
        int i = 0;
        for (var key : reviews.keys()) {
            var reviewsForItem = reviews.get(key);
            itemReviewCounts[i] = reviewsForItem.size();
            i++;
        }

        return new StatsBundle(itemReviewCounts);
    }

    public static StatsBundle reviewScores(DataSet ds) {
        var users = ds.getUsers();
        var reviewCount = 0;
        for (var user : users) {
            var reviews = user.getReviews();
            reviewCount += reviews.size();
        }

        double[] scores = new double[reviewCount];
        int i = 0;
        for (var user : users) {
            var reviews = user.getReviews();
            for (var review : reviews) {
                scores[i] = review.getStars();
                i++;
            }
        }

        return new StatsBundle(scores);
    }


    private static double mean(double[] items) {
        int count = 0;
        double sum = 0;
        for (var item : items) {
            count++;
            sum += item;
        }
        return sum / count;
    }

    private static double median(double[] items) {
        Arrays.sort(items);
        int midIndex = (items.length / 2);
        return items[midIndex];
    }

    private static double mode(double[] items) {
        HashMap<Double, Integer> counts = new HashMap<>();
        for (var item : items) {
            var currentValue = counts.getOrDefault(item, 0);
            counts.put(item, currentValue + 1);
        }

        double maxKey = 0;
        int maxVal = 0;
        for (var key : counts.keySet()) {
            var val = counts.get(key);
            if (val > maxVal) {
                maxVal = val;
                maxKey = key;
            }
        }
        return maxKey;
    }

    private static double min(double[] items) {
        double min = Double.MAX_VALUE;
        for (var item : items) {
            if (item < min) {
                min = item;
            }
        }
        return min;
    }

    private static double max(double [] items) {
        double max = Double.MIN_VALUE;
        for (var item : items) {
            if (item > max) {
                max = item;
            }
        }
        return max;
    }
}
