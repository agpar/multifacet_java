package agpar.multifacet;

import agpar.multifacet.commands.Command;
import agpar.multifacet.commands.CommandBuilder;
import agpar.multifacet.commands.CommandError;
import agpar.multifacet.experiments.*;
import agpar.multifacet.pairwise_features.runners.GenerateAllPairwise;
import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import agpar.multifacet.recommend.RatingTupleGenerator;
import agpar.multifacet.recommend.data_sharing.SharedDataModel;
import agpar.multifacet.single_features.GenerateAllSingle;
import net.librec.common.LibrecVersion;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;


public class Main {
    private static DATA_SOURCE SOURCE;

    public static void main(String[] args) throws Exception {
        if (!LibrecVersion.VERSION.equals("2.0.0-agpar-fork")) {
            throw new Exception("Not using forked librec version!");
        }

        CommandBuilder cb = new CommandBuilder(args);
        SOURCE = cb.dataSource();
        Command command = cb.build();

        try {
            command.runCommand();
        }
        finally {
            ResultWriter.closeAllSingletons();
        }
    }

    // TODO consider moving this.
    public static DATA_SOURCE get_source() {
        return Main.SOURCE;
    }
}
