package agpar.multifacet;

import agpar.multifacet.commands.Command;
import agpar.multifacet.commands.CommandBuilder;
import agpar.multifacet.data_interface.DATA_SOURCE;
import agpar.multifacet.pairwise_features.io.ResultWriter;
import net.librec.common.LibrecVersion;


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
