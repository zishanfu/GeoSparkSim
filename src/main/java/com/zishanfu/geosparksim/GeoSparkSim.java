package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Generation.CreateVehicles;
import com.zishanfu.geosparksim.Interaction.Jmap;
import com.zishanfu.geosparksim.Model.*;
import com.zishanfu.geosparksim.OSM.OsmParser;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.TrafficUI.TrafficPanel;
import com.zishanfu.geosparksim.osm.*;
import com.zishanfu.geosparksim.tools.HDFSUtil;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


@Command(version = "GeoSparkSim v0.0.1", header = "%nGeoSparkSim Command Help%n",
        description = "Prints usage help and version help when requested.%n")
public class GeoSparkSim implements Runnable{
    //hdfs://localhost:9000
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

    @Option(names = {"-r", "--rep"}, type = int.class, description = "The repartition steps. Default value: ${DEFAULT-VALUE}", defaultValue = "120")
    private int rep;

    @Option(names = {"-t", "--timestep"}, type = double.class, description = "Time per step. Default value: ${DEFAULT-VALUE}", defaultValue = "1")
    private double timestep;

    @Option(names = {"-f", "--fileoutput"}, description = "Output file path.")
    private String output;

    @Option(names = {"-p", "--partition"}, type = int.class, description = "The number of data partitions. Default value: ${DEFAULT-VALUE}", defaultValue = "100")
    private int partition;

    @Option(names = {"-y", "--type"}, description = "Vehicle generation type. (DSO or NB) Default value: ${DEFAULT-VALUE}", defaultValue = "DSO")
    private String type;

    @Option(names = {"-o", "--only"}, description = "Only run the interactive interface.")
    private boolean only;

    @Option(names = {"-c", "--command"}, description = "Run all parameters in command line (experiment).")
    private boolean command;

    @Option(names = {"-d", "--distributed"}, description = "Run in distributed mode.")
    private boolean dist;

    @Option(names = {"-m", "--manuscript"}, description = "Manuscript path.")
    private String manuscript;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
    private boolean versionHelpRequested;

    private final Properties prop = new Properties();
    private final String appTitle = "GeoSparkSim v0.0.1";

    @Override
    public void run() {
        SparkSession spark;

        if (dist){
            spark = SparkSession
                    .builder()
                    .appName("GeoSparkSim")
                    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                    .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                    .getOrCreate();
        }else{
            spark = SparkSession
                    .builder()
                    .master("local[*]")
                    .appName("GeoSparkSim")
                    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                    .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                    .getOrCreate();
        }

        if(only){
            Jmap jmap = new Jmap(appTitle);
            jmap.runUI(spark);
        }else if(command){
            if(output == null){
                LOG.error("Please enter the output path and rerun the command.");
            }else{
                try {
                    run(spark, lat1, lon1, lat2, lon2, num, output, step, timestep, type, rep, partition);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if(manuscript != null){
            InputStream is = null;
            try {
                is = new FileInputStream(manuscript);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                prop.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            double geo1Lat = Double.parseDouble(prop.getProperty("geo1.lat"));
            double geo1Lon = Double.parseDouble(prop.getProperty("geo1.lon"));
            double geo2Lat = Double.parseDouble(prop.getProperty("geo2.lat"));
            double geo2Lon = Double.parseDouble(prop.getProperty("geo2.lon"));
            String selectedType = prop.getProperty("vehicle.type");
            int total = Integer.parseInt(prop.getProperty("vehicle.num"));
            double timestep = Double.parseDouble(prop.getProperty("simulation.timestep"));
            int steps = Integer.parseInt(prop.getProperty("simulation.step"));
            int repartition = Integer.parseInt(prop.getProperty("simulation.repartition"));
            String simOutput = prop.getProperty("simulation.output");
            try {
                run(spark, geo1Lat, geo1Lon, geo2Lat, geo2Lon, total, simOutput, steps, timestep, selectedType, repartition, partition);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            System.getProperty("user.dir");
            try {
                run(spark, lat1, lon1, lat2, lon2, num, output, step, timestep, type, rep, partition);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        CommandLine.run(new GeoSparkSim(), System.err, args);
    }

    private void run(SparkSession spark, double lat1, double lon1, double lat2, double lon2,
                     int total, String outputPath, int step, double timestep, String type, int repartition, int partition) throws ExecutionException, InterruptedException {
        String str = "\nP1: " + lat1 + ", " + lon1 + "\n" + "P2: " + lat2 + ", " + lon2 + "\n" + "Total: " + total + "\n" + "Steps: " + step + "\n" + "Timestep: " + timestep
                + "\n" + "Generation Type: " + type + "\n" + "Repartition Time: " + repartition + "\n" + "Partition: " + partition + "\n" + "Output: " + output + "\n";
        LOG.warn(str);

        HDFSUtil hdfs = new HDFSUtil(outputPath);
        String name = "/geosparksim";
        hdfs.deleteDir(name);
        hdfs.mkdir(name);
        output = outputPath + name;
        OsmParser osmParser = new OsmParser();
        Coordinate coor1 = new Coordinate(lat1, lon1);
        Coordinate coor2 = new Coordinate(lat2, lon2);
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(lat1 + maxLen, lon1 - maxLen);
        Coordinate newCoor2 = new Coordinate(lat2 + maxLen, lon2 - maxLen);

        osmParser.runInHDFS(newCoor1, newCoor2, output, System.getProperty("user.dir"));

        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, output);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, output);
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();

        String osmPath = "datareader.file=" + System.getProperty("user.dir") + "/map.osm";
        String[] vehParameters = new String[]{"config=" + System.getProperty("user.dir") + "/config.properties", osmPath};

        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = createVehicles.multiple(total, type);

        VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));

        double area = rectArea(lat1, lon1, lat2, lon2);
        simulation(spark, total, output, step, timestep, repartition, partition, area);
    }


    private void simulation(SparkSession spark, int total, String path, int step, double timestep, int repartition, int partition, double area){
        VehicleHandler vehicleHandler = new VehicleHandler(spark, path);

        RoadNetworkReader networkReader = new RoadNetworkReader(spark, path);
        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
        LOG.warn("Read: edge: " + edges.count() + ", signals: " + signals.count() + ", intersects: " + intersects.count() + ", vehicles: " + vehicles.count());

        long t1 = System.currentTimeMillis();
        Microscopic.sim(spark, edges, signals, intersects, vehicles, path, step, timestep, repartition, partition);
        long t2 = System.currentTimeMillis();
        LOG.warn("Finished Simulation: " + (t2- t1) / 1000);

        ReportHandler reportHandler = new ReportHandler(spark, path, partition);
        Dataset<StepReport> reports = reportHandler.readReportJson();

        if(total < 5000 && area < 8000000){
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports.collectAsList());
        }else{
            LOG.error("Because the number of vehicle is larger than 5000 or the area is larger than 800,0000, " +
                    "GeoSparkSim will not show the traffic visualization! Please check output in " + path);
        }
    }

    private double rectArea(double lat1, double lon1, double lat2, double lon2) {
        double length = haversine(new Coordinate(lat1, lon2), new Coordinate(lat1, lon1));
        double height = haversine(new Coordinate(lat2, lon1), new Coordinate(lat2, lon2));
        return(length * height);
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }

    private double haversine(Coordinate coor1, Coordinate coor2) {
        double latitude1 = Math.toRadians(coor1.x);
        double latitude2 = Math.toRadians(coor2.x);
        double longitude1 = Math.toRadians(coor1.y);
        double longitude2 = Math.toRadians(coor2.y);

        return (1000 * 2 * 6371 * Math.asin(Math.sqrt(
                Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
                        Math.cos(latitude1) * Math.cos(latitude2) *
                                Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2))));
    }

}
