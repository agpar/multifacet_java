package com.company.pairwise;

import java.util.List;

public class ReviewSimilarity {
    public double pcc(List<Double> scores1, List<Double> avgs1,
                      List<Double> scores2, List<Double> avgs2) {
        assert(scores1.size() == avgs1.size());
        assert(avgs1.size() == scores2.size());
        assert(scores2.size() == avgs2.size());
        if (scores1.size() == 0) return 0;

        double numer = 0.0;
        double denom1 = 0.0;
        double denom2 = 0.0;
        for(int i = 0; i < scores1.size(); i++) {
            Double score1 = scores1.get(i);
            Double avg1 = avgs1.get(i);
            Double score2 = scores2.get(i);
            Double avg2 = avgs2.get(i);

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
