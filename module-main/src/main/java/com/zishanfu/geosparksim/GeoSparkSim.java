package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.interaction.Jmap;
import com.zishanfu.geosparksim.model.SimConfig;
import com.zishanfu.geosparksim.simulation.Core;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * GeoSparkSim main entrance. There are three ways to launch the application.<br>
 * 1. User interface. -o will start application interface.<br>
 * 2. Manuscript. Specify manuscript path with -m will start the application as the manuscript mode.
 * <br>
 * 3. Command. -c will start the application in command mode. This mode usually uses in experiments.
 * <br>
 * By default, if a user doesn't enter anything, the application will start as the command mode with
 * the default value.<br>
 * Top-left: (33.48998, -112.10964), bottom-right: (33.38827, -111.79722), the number of vehicle:
 * 10000, the number of step: 600, the time per step: 1 second, the vehicle generation type:
 * Data-space oriented approach(DSO).<br>
 * The top-left and bottom-right coordinates with (latitude, longitude) format define the boundary
 * of the simulation. For example, Arizona State University(ASU) boundary is top-left 33.429165,
 * -111.942323 and bottom-right 33.413572, -111.924442. <br>
 * <strong>Note: </strong><br>
 * A output path is required in any one of the modes.<br>
 * By default, the application is in standalone mode. You will need to set a new master in
 * distributed mode.
 *
 * @see <a href="https://github.com/remkop/picocli">picocli</a>
 * @see <a
 *     href="https://iapg.jade-hs.de/files/iapg/mitglieder/brinkhoff/paper/GeoInformatica2002.pdf">A
 *     Framework for Generating Network-Based Moving Objects</a>
 */
@Command(
        version = "GeoSparkSim v0.0.1",
        header = "%nGeoSparkSim Command Help%n",
        description = "Prints usage help and version help when requested.%n")
public class GeoSparkSim implements Runnable {

    @Option(
            names = {"-lt1", "--lat1"},
            type = double.class,
            description = "Latitude 1. Default value: ${DEFAULT-VALUE}",
            defaultValue = "33.48998")
    private double lat1;

    @Option(
            names = {"-ln1", "--lon1"},
            type = double.class,
            description = "Longitude 1. Default value: ${DEFAULT-VALUE}",
            defaultValue = "-112.10964")
    private double lon1;

    @Option(
            names = {"-lt2", "--lat2"},
            type = double.class,
            description = "Latitude 2. Default value: ${DEFAULT-VALUE}",
            defaultValue = "33.38827")
    private double lat2;

    @Option(
            names = {"-ln2", "--lon2"},
            type = double.class,
            description = "Longitude 2. Default value: ${DEFAULT-VALUE}",
            defaultValue = "-111.79722")
    private double lon2;

    @Option(
            names = {"-n", "--num"},
            type = int.class,
            description = "The number of vehicles. Default value: ${DEFAULT-VALUE}",
            defaultValue = "10000")
    private int num;

    @Option(
            names = {"-s", "--step"},
            type = int.class,
            description = "The simulation steps. Default value: ${DEFAULT-VALUE}",
            defaultValue = "600")
    private int step;

    @Option(
            names = {"-t", "--timestep"},
            type = double.class,
            description = "Time per step. Default value: ${DEFAULT-VALUE}",
            defaultValue = "1")
    private double timestep;

    @Option(
            names = {"-f", "--fileoutput"},
            description = "Output file path.")
    private String output;

    @Option(
            names = {"-y", "--type"},
            description = "Vehicle generation type. (DSO or NB) Default value: ${DEFAULT-VALUE}",
            defaultValue = "DSO")
    private String type;

    @Option(
            names = {"-o", "--only"},
            description = "Only run the interactive interface.")
    private boolean only;

    @Option(
            names = {"-c", "--command"},
            description = "Run all parameters in command line (experiment).")
    private boolean command;

    @Option(
            names = {"-m", "--manuscript"},
            description = "Manuscript path.")
    private String manuscript;

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @Option(
            names = {"-V", "--version"},
            versionHelp = true,
            description = "Print version information and exit.")
    private boolean versionHelpRequested;

    private static final Logger LOG = Logger.getLogger(GeoSparkSim.class);
    private static final Properties prop = new Properties();
    private static final String appTitle = "GeoSparkSim v0.0.1";

    @Override
    public void run() {
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);

        SparkSession spark =
                SparkSession.builder()
                        .master("local[*]") // Developing mode
                        .appName("GeoSparkSim")
                        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                        .config(
                                "spark.kryo.registrator",
                                "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                        .getOrCreate();

        SimConfig simConfig = new SimConfig();

        // User interface
        if (only) {
            Jmap jmap = new Jmap(appTitle);
            jmap.runUI(spark);
            // Command
        } else if (command) {
            if (output == null) {
                LOG.warn("Please enter the output path and rerun the command.");
            } else if (num < 1000) {
                LOG.warn("Please enter a vehicle number not smaller than 1000.");
            } else {
                simConfig =
                        new SimConfig(lat1, lon1, lat2, lon2, num, output, step, timestep, type);
                start(spark, simConfig);
            }

            // Manuscript
        } else if (manuscript != null) {
            InputStream is = null;
            try {
                is = new FileInputStream(manuscript);
            } catch (FileNotFoundException e) {
                LOG.error("Manuscript can't be found.", e);
            }

            // Load properties from manuscript
            try {
                prop.load(is);
            } catch (IOException e) {
                LOG.error("Error happens when loading properties from manuscript.", e);
            }

            simConfig.setLat1(Double.parseDouble(prop.getProperty("geo1.lat")));
            simConfig.setLon1(Double.parseDouble(prop.getProperty("geo1.lon")));
            simConfig.setLat2(Double.parseDouble(prop.getProperty("geo2.lat")));
            simConfig.setLon2(Double.parseDouble(prop.getProperty("geo2.lon")));
            simConfig.setType(prop.getProperty("vehicle.type"));
            simConfig.setTotal(Integer.parseInt(prop.getProperty("vehicle.num")));
            simConfig.setTimestep(Double.parseDouble(prop.getProperty("simulation.timestep")));
            simConfig.setStep(Integer.parseInt(prop.getProperty("simulation.step")));
            simConfig.setOutputPath(prop.getProperty("simulation.output"));

            start(spark, simConfig);

            // Default setting
        } else {
            start(spark, simConfig);
        }
    }

    public static void main(String[] args) {
        CommandLine.run(new GeoSparkSim(), System.err, args);
    }

    /**
     * Start run GeoSparkSim.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     */
    private void start(SparkSession spark, SimConfig simConfig) {
        Core core = new Core();
        core.preprocess(spark, simConfig);
        core.simulation(spark, simConfig, appTitle);
    }
}
