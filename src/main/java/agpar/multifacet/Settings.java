package agpar.multifacet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {

    private String YELP_DATA_DIR;
    private String PYTHON_PROJECT_DIR;
    private String EPINIONS_DATA_DIR;

    private static Settings settings;

    private Settings() {
        String settings_path = settingFilePath();
        String multifacet_root = multifacetRoot();
        Properties prop = new Properties();
        try {
            prop.load(new FileReader(settings_path));
            this.YELP_DATA_DIR = prop.getProperty("yelp_data_dir");
            this.EPINIONS_DATA_DIR = prop.getProperty("epinions_data_dir");
            this.PYTHON_PROJECT_DIR = Path.of(multifacet_root, "src/main/python").toString();
        } catch (FileNotFoundException e) {
            System.out.printf("ERROR: Could not find settings file at %s\n", settings_path);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("ERROR: Failed to close settings file.");
            System.exit(1);
        }
    }

    private static String settingFilePath() {
        String multifacet_root = multifacetRoot();
        String settings_default_path = Path.of(multifacet_root, "settings.properties").toString();
        String settings_local_path = Path.of(multifacet_root, "settings_local.properties").toString();
        if (new File(settings_local_path).exists()) {
            return settings_local_path;
        }
        return settings_default_path;
    }

    private static String multifacetRoot() {
        String multifacet_root = System.getenv("MULTIFACET_ROOT");
        if (multifacet_root == null) {
            System.out.println("ERROR: MULTIFACET_ROOT is not defined.");
            System.exit(1);
        }
        return multifacet_root;
    }

    private static void init() {
        if (settings == null) {
            settings = new Settings();
        }
    }

    public static String YELP_DATA_DIR() {
        init();
        return settings.YELP_DATA_DIR;
    }

    public static String PYTHON_PROJECT_DIR() {
        init();
        return settings.PYTHON_PROJECT_DIR;
    }

    public static String EPINIONS_DATA_DIR() {
        init();
        return settings.EPINIONS_DATA_DIR;
    }
}
