package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.model.*;
import com.zishanfu.geosparksim.osm.*;
import com.zishanfu.geosparksim.tools.FileOps;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.*;

public class SimulationTester extends GeoSparkSimTestBase {
    private JavaSparkContext sc;
    private SparkSession ss;
    private String resources;
    private FileOps fileOps = new FileOps();

    @Before
    public void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("Simulation").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        String path =
                SimulationTester.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath();
        int idx = path.indexOf("/target");
        resources = path.substring(0, idx) + "/src/test/resources";
        fileOps.createDirectory(resources + "/java-test");
    }

    @After
    public void tearDown() {
        fileOps.deleteDirectory(resources + "/java-test");
        sc.stop();
    }

    @Test
    public void simulation() {
        String path = resources + "/samples";
        VehicleHandler vehicleHandler = new VehicleHandler(ss, path);
        RoadNetworkReader networkReader = new RoadNetworkReader(ss, path);

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();

        Microscopic.sim(
                ss, edges, signals, intersects, vehicles, resources + "/java-test", 600, 1, 10);

        ReportHandler reportHandler = new ReportHandler(ss, resources + "/java-test", 1);
        Dataset<StepReport> reports = reportHandler.readReportJson();
        reports.show();
    }
}
