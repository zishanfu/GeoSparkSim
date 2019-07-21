package com.zishanfu.geosparksim.Simulation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Generation.CreateVehicles;
import com.zishanfu.geosparksim.Microscopic;
import com.zishanfu.geosparksim.Model.*;
import com.zishanfu.geosparksim.OSM.OsmParser;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.TrafficUI.TrafficPanel;
import com.zishanfu.geosparksim.osm.*;
import com.zishanfu.geosparksim.tools.HDFSUtil;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Core {

    private final static Logger LOG = Logger.getLogger(Core.class);

    /**
     * Preprocess road network and generate vehicles
     *
     * @param spark the spark session
     * @param entry the program entry
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void preprocess(SparkSession spark, Entry entry) throws ExecutionException, InterruptedException {
        LOG.warn(entry.toString());

        HDFSUtil hdfs = new HDFSUtil(entry.getOutputPath());
        String name = "/geosparksim";
        hdfs.deleteDir(name);
        hdfs.mkdir(name);
        String output = entry.getOutputPath() + name;

        OsmParser osmParser = new OsmParser();
        Coordinate coor1 = new Coordinate(entry.getLat1(), entry.getLon1());
        Coordinate coor2 = new Coordinate(entry.getLat2(), entry.getLon2());
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(entry.getLat1() + maxLen, entry.getLon1() - maxLen);
        Coordinate newCoor2 = new Coordinate(entry.getLat2() + maxLen, entry.getLon2() - maxLen);

        osmParser.runInHDFS(newCoor1, newCoor2, output);

        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, output);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, output);
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();

        String osmPath = "datareader.file=" + System.getProperty("user.dir") + "/map.osm";
        String[] vehParameters = new String[]{"config=" + System.getProperty("user.dir") + "/config.properties", osmPath};

        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = createVehicles.multiple(entry.getTotal(), entry.getType());

        VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
    }

    //public void simulation(SparkSession spark, int total, String path, int step, double timestep, int repartition, int partition, double area)
    public void simulation(SparkSession spark, Entry entry, String appTitle){
        VehicleHandler vehicleHandler = new VehicleHandler(spark, entry.getOutputPath());
        RoadNetworkReader networkReader = new RoadNetworkReader(spark, entry.getOutputPath());

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
        LOG.warn("Read: edge: " + edges.count() + ", signals: " + signals.count() + ", intersects: " + intersects.count() + ", vehicles: " + vehicles.count());

        long t1 = System.currentTimeMillis();
        Microscopic.sim(spark, edges, signals, intersects, vehicles,
                entry.getOutputPath(), entry.getStep(), entry.getTimestep(), entry.getRepartition(), entry.getPartition());
        long t2 = System.currentTimeMillis();
        LOG.warn("Finished Simulation: " + (t2- t1) / 1000);

        ReportHandler reportHandler = new ReportHandler(spark, entry.getOutputPath(), entry.getPartition());
        Dataset<StepReport> reports = reportHandler.readReportJson();

        double area = new Distance().rectArea(entry.getLat1(), entry.getLon1(), entry.getLat2(), entry.getLon2());

        //Visualization restriction
        if(entry.getTotal() < 5000 && area < 8000000){
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports.collectAsList());
        }else{
            LOG.error("Because the number of vehicle is larger than 5000 or the area is larger than 800,0000, " +
                    "GeoSparkSim will not show the traffic visualization! Please check output in " + entry.getOutputPath());
        }
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }

}
