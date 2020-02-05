package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.stats.DataStatsCalculator;

public class GenerateStats implements Command {
    private DATA_SOURCE source;

    public GenerateStats(DATA_SOURCE source) {
        this.source = source;
    }

    @Override
    public void runCommand() {
        DataSet ds = DataSet.getInstance();
        ds.load(0, DEFAULT_USER_UPPER_BOUND);

        System.out.printf("Generated stats for %s dataset.\n", source.toString());

        System.out.print("User Review Counts\n");
        System.out.printf("%s\n", DataStatsCalculator.userReviewCount(ds).toString());

        System.out.print("User Average Review Scores\n");
        System.out.printf("%s\n", DataStatsCalculator.userReviewAverages(ds).toString());

        System.out.print("User Friend Counts\n");
        System.out.printf("%s\n", DataStatsCalculator.friendsPerUser(ds).toString());

        System.out.print("Item Review Counts\n");
        System.out.printf("%s\n", DataStatsCalculator.itemReviewCount(ds).toString());

        System.out.print("Global Review scores\n");
        System.out.printf("%s\n", DataStatsCalculator.reviewScores(ds).toString());
    }
}
