package com.zishanfu.geosparksim.simulation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Microscopic;
import com.zishanfu.geosparksim.generation.CreateVehicles;
import com.zishanfu.geosparksim.model.*;
import com.zishanfu.geosparksim.model.SimConfig;
import com.zishanfu.geosparksim.osm.*;
import com.zishanfu.geosparksim.osm.OsmLoader;
import com.zishanfu.geosparksim.tools.Distance;
import com.zishanfu.geosparksim.tools.HDFSUtil;
import com.zishanfu.geosparksim.trafficUI.TrafficPanel;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;
import scala.collection.Seq;

/** Core class to do data pre-processing and simulation. */
public class Core {
    private static final Logger LOG = Logger.getLogger(Core.class);

    /**
     * Pre-process road network and generate vehicles.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     */
    public void preprocess(SparkSession spark, SimConfig simConfig) {
        String path = Core.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int idx = path.indexOf("/target");
        LOG.warn(simConfig.toString());

        HDFSUtil hdfs = new HDFSUtil(simConfig.getOutputPath());
        String name = "geosparksim";
        hdfs.deleteDir(name);
        hdfs.mkdir(name);
        String output = simConfig.getOutputPath() + name;
        LOG.warn("output: " + output);

        Coordinate coor1 = new Coordinate(simConfig.getLat1(), simConfig.getLon1());
        Coordinate coor2 = new Coordinate(simConfig.getLat2(), simConfig.getLon2());
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

        String osmPath = output + "/map.osm";

        CreateVehicles createVehicles = new CreateVehicles(osmPath, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = null;
        try {
            vehicleList = createVehicles.multiple(simConfig.getTotal(), simConfig.getType());
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Bad thing happens when generating vehicle list, try again!", e);
        }

        VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
    }

    /**
     * Take processed data, run the simulation and open visualization panel when the simulation is
     * finished.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     * @param appTitle the app title
     */
    public void simulation(SparkSession spark, SimConfig simConfig, String appTitle) {
        String path = simConfig.getOutputPath() + "geosparksim";
        VehicleHandler vehicleHandler = new VehicleHandler(spark, path);
        RoadNetworkReader networkReader = new RoadNetworkReader(spark, path);

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
        LOG.info(
                "Read: edge: "
                        + edges.count()
                        + ", signals: "
                        + signals.count()
                        + ", intersects: "
                        + intersects.count()
                        + ", vehicles: "
                        + vehicles.count());

        long t1 = System.currentTimeMillis();
        Microscopic.sim(
                spark,
                edges,
                signals,
                intersects,
                vehicles,
                path,
                simConfig.getStep(),
                simConfig.getTimestep(),
                simConfig.getPartition(),
                simConfig.isOutputSignal());
        long t2 = System.currentTimeMillis();
        LOG.info("Finished Simulation: " + (t2 - t1) / 1000);

        ReportHandler reportHandler = new ReportHandler(spark, path, simConfig.getPartition());
        Dataset<StepReport> reports = reportHandler.readReportJson();

        double area =
                new Distance()
                        .rectArea(
                                simConfig.getLat1(),
                                simConfig.getLon1(),
                                simConfig.getLat2(),
                                simConfig.getLon2());

        // Visualization restriction
        if (simConfig.getTotal() < 5000 && area < 8000000) {
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports.collectAsList());
        } else {
            LOG.info(
                    "Because the number of vehicle is larger than 5000 or the area is larger than 800,0000, "
                            + "GeoSparkSim will not show the traffic visualization! Please check output in "
                            + simConfig.getOutputPath());
        }
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }
}
