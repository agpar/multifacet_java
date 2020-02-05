package agpar.multifacet.commands;

import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.data_interface.DataSet;
import agpar.multifacet.stats.DataStatsCalculator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        printAndWrite("User Review Counts", DataStatsCalculator.userReviewCount(ds));
        printAndWrite("User Average Review Scores", DataStatsCalculator.userReviewAverages(ds));
        printAndWrite("UserFriendCount", DataStatsCalculator.friendsPerUser(ds));
        printAndWrite("Item Review Counts", DataStatsCalculator.itemReviewCount(ds));
        printAndWrite("Global Review Scores", DataStatsCalculator.reviewScores(ds));
    }

    private void printAndWrite(String title, DataStatsCalculator.StatsBundle stats) {
        String outputFile = "data_" + title.toLowerCase().replace(" ", "_") + ".txt";

        System.out.println(title);
        System.out.printf("%s\n", stats.toString());

        try {
            Writer writer = new BufferedWriter(new FileWriter(outputFile));
            List<String> vals = Stream.of(stats.values).map(Object::toString).collect(Collectors.toList());
            writer.write(String.join(",", vals));
            writer.close();
        } catch (IOException e) {
            System.out.printf("Failed to output data to %s", outputFile);
        }
    }
}
