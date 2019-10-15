package com.zishanfu.geosparksim.simulation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.model.Entry;
import com.zishanfu.geosparksim.generation.CreateVehicles;
import com.zishanfu.geosparksim.Microscopic;
import com.zishanfu.geosparksim.model.*;
import com.zishanfu.geosparksim.osm.OsmLoader;
import com.zishanfu.geosparksim.tools.Distance;
import com.zishanfu.geosparksim.trafficUI.TrafficPanel;
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
    private final String resources = System.getProperty("user.dir") + "/src/test/resources";

    /**
     * Preprocess road network and generate vehicles
     *
     * @param spark the spark session
     * @param entry the program entry
     */
    public void preprocess(SparkSession spark, Entry entry) {
        LOG.warn(entry.toString());

        HDFSUtil hdfs = new HDFSUtil(entry.getOutputPath());
        String name = "/geosparksim";
        hdfs.deleteDir(name);
        hdfs.mkdir(name);
        String output = entry.getOutputPath() + name;

        Coordinate coor1 = new Coordinate(entry.getLat1(), entry.getLon1());
        Coordinate coor2 = new Coordinate(entry.getLat2(), entry.getLon2());
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(coor1.x + maxLen, coor1.y - maxLen);
        Coordinate newCoor2 = new Coordinate(coor2.x + maxLen, coor2.y - maxLen);

        OsmLoader osmLoader = new OsmLoader(newCoor1, newCoor2, output);

        osmLoader.parquet();
        osmLoader.osm();

        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, output);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, output);
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();

        String osmPath = "datareader.file=" + output + "/map.osm";
        String[] vehParameters = new String[]{"config=" + resources + "/graphhopper/config.properties", osmPath};

        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = null;
        try {
            vehicleList = createVehicles.multiple(entry.getTotal(), entry.getType());
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Bad thing happens when generating vehicle list, try again!", e);
        }

        VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
    }

    public void simulation(SparkSession spark, Entry entry, String appTitle){
        String path = entry.getOutputPath() + "/geosparksim";
        VehicleHandler vehicleHandler = new VehicleHandler(spark, path);
        RoadNetworkReader networkReader = new RoadNetworkReader(spark, path);

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
        LOG.warn("Read: edge: " + edges.count() + ", signals: " + signals.count() + ", intersects: " + intersects.count() + ", vehicles: " + vehicles.count());

        long t1 = System.currentTimeMillis();
        Microscopic.sim(spark, edges, signals, intersects, vehicles,
                path, entry.getStep(), entry.getTimestep(), entry.getPartition());
        long t2 = System.currentTimeMillis();
        LOG.warn("Finished Simulation: " + (t2- t1) / 1000);

        ReportHandler reportHandler = new ReportHandler(spark, path, entry.getPartition());
        Dataset<StepReport> reports = reportHandler.readReportJson();

        double area = new Distance().rectArea(entry.getLat1(), entry.getLon1(), entry.getLat2(), entry.getLon2());

        //Visualization restriction
        if(entry.getTotal() < 5000 && area < 8000000){
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports.collectAsList());
        }else{
            LOG.warn("Because the number of vehicle is larger than 5000 or the area is larger than 800,0000, " +
                    "GeoSparkSim will not show the traffic visualization! Please check output in " + entry.getOutputPath());
        }
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }
}
