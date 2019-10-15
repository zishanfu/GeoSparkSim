package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.interaction.Jmap;
import com.zishanfu.geosparksim.model.Entry;
import com.zishanfu.geosparksim.simulation.Core;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Command(version = "GeoSparkSim v0.0.1", header = "%nGeoSparkSim Command Help%n",
        description = "Prints usage help and version help when requested.%n")
public class GeoSparkSim implements Runnable{

    private final static Logger LOG = Logger.getLogger(GeoSparkSim.class);

    @Option(names = {"-lt1", "--lat1"}, type = double.class, description = "Latitude 1. Default value: ${DEFAULT-VALUE}", defaultValue = "33.48998")
    private double lat1;

    @Option(names = {"-ln1", "--lon1"}, type = double.class, description = "Longitude 1. Default value: ${DEFAULT-VALUE}", defaultValue = "-112.10964")
    private double lon1;

    @Option(names = {"-lt2", "--lat2"}, type = double.class, description = "Latitude 2. Default value: ${DEFAULT-VALUE}", defaultValue = "33.38827")
    private double lat2;

    @Option(names = {"-ln2", "--lon2"}, type = double.class, description = "Longitude 2. Default value: ${DEFAULT-VALUE}", defaultValue = "-111.79722")
    private double lon2;

    @Option(names = {"-n", "--num"}, type = int.class, description = "The number of vehicles. Default value: ${DEFAULT-VALUE}", defaultValue = "10000")
    private int num;

    @Option(names = {"-s", "--step"}, type = int.class, description = "The simulation steps. Default value: ${DEFAULT-VALUE}", defaultValue = "600")
    private int step;

    @Option(names = {"-t", "--timestep"}, type = double.class, description = "Time per step. Default value: ${DEFAULT-VALUE}", defaultValue = "1")
    private double timestep;

    @Option(names = {"-f", "--fileoutput"}, description = "Output file path.")
    private String output;

    @Option(names = {"-y", "--type"}, description = "Vehicle generation type. (DSO or NB) Default value: ${DEFAULT-VALUE}", defaultValue = "DSO")
    private String type;

    @Option(names = {"-o", "--only"}, description = "Only run the interactive interface.")
    private boolean only;

    @Option(names = {"-c", "--command"}, description = "Run all parameters in command line (experiment).")
    private boolean command;

    @Option(names = {"-m", "--manuscript"}, description = "Manuscript path.")
    private String manuscript;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
    private boolean versionHelpRequested;

    private final Properties prop = new Properties();
    private final String appTitle = "GeoSparkSim v0.0.1";

    //ASU boundary
    //top-left 33.429165, -111.942323
    //bottom-right 33.413572, -111.924442

    @Override
    public void run() {

        SparkSession spark = SparkSession
                .builder()
                .master("local[*]") //Developing mode
                .appName("GeoSparkSim")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                .getOrCreate();

        Entry entry = new Entry();

        //User interface
        if(only){
            Jmap jmap = new Jmap(appTitle);
            jmap.runUI(spark);
        //Command
        }else if(command){
            if(output == null){
                LOG.warn("Please enter the output path and rerun the command.");
            }else if(num < 1000){
                LOG.warn("Please enter a vehicle number not smaller than 1000.");
            }else{
                entry = new Entry(lat1, lon1, lat2, lon2, num, output, step, timestep, type);
                start(spark, entry);
            }

        //Manuscript
        }else if(manuscript != null){
            InputStream is = null;
            try {
                is = new FileInputStream(manuscript);
            } catch (FileNotFoundException e) {
                LOG.warn("Manuscript can't be found.", e);
            }

            //Load properties from manuscript
            try {
                prop.load(is);
            } catch (IOException e) {
                LOG.warn("Error happens when loading properties from manuscript.", e);
            }

            entry.setLat1(Double.parseDouble(prop.getProperty("geo1.lat")));
            entry.setLon1(Double.parseDouble(prop.getProperty("geo1.lon")));
            entry.setLat2(Double.parseDouble(prop.getProperty("geo2.lat")));
            entry.setLon2(Double.parseDouble(prop.getProperty("geo2.lon")));
            entry.setType(prop.getProperty("vehicle.type"));
            entry.setTotal(Integer.parseInt(prop.getProperty("vehicle.num")));
            entry.setTimestep(Double.parseDouble(prop.getProperty("simulation.timestep")));
            entry.setStep(Integer.parseInt(prop.getProperty("simulation.step")));
            entry.setOutputPath(prop.getProperty("simulation.output"));

            start(spark, entry);

        //Default setting
        }else{
            start(spark, entry);
        }
    }

    public static void main(String[] args){
        CommandLine.run(new GeoSparkSim(), System.err, args);
    }

    private void start(SparkSession spark, Entry entry){
        Core core = new Core();
        core.preprocess(spark, entry);
        core.simulation(spark, entry, appTitle);
    }
}
