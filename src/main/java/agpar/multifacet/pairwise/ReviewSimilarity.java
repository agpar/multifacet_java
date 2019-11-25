package agpar.multifacet.pairwise;

import agpar.multifacet.data_interface.data_classes.Review;

public class ReviewSimilarity {
    public static double pcc(Review[] reviews1, double[] avgs1,
                             Review[] reviews2, double[] avgs2) {
        assert(reviews1.length == avgs1.length);
        assert(avgs1.length == reviews2.length);
        assert(reviews2.length == avgs2.length);
        if (reviews1.length == 0) return 0;

        double numer = 0.0;
        double denom1 = 0.0;
        double denom2 = 0.0;
        for(int i = 0; i < reviews1.length; i++) {
            assert(reviews1[i].getItemIdInt() == reviews2[i].getItemIdInt());
            double score1 = reviews1[i].getStars();
            double avg1 = avgs1[i];
            double score2 = reviews2[i].getStars();
            double avg2 = avgs2[i];

            numer += (score1 - avg1) * (score2 - avg2);
            denom1 += Math.pow((score1 - avg1), 2);
            denom2 += Math.pow((score2 - avg2), 2);
        }
        double denom = Math.sqrt(denom1 * denom2);
        if (denom == 0) {
            return 0;
        } else {
            return numer / denom;
        }
    }
}
