package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.model.Link;
import com.zishanfu.geosparksim.model.StepReport;
import com.zishanfu.geosparksim.trafficUI.TrafficPanel;
import com.zishanfu.geosparksim.osm.ReportHandler;
import com.zishanfu.geosparksim.osm.RoadNetworkReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VisualizationTester extends GeoSparkSimTestBase{
    static JavaSparkContext sc;
    static SparkSession ss;
    static String resources;

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("Visualization").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
    }

    @AfterClass
    public static void tearDown()
    {
        sc.stop();
    }

    @Test
    public void visualization()
    {
        String path = resources + "/samples";
        RoadNetworkReader networkReader = new RoadNetworkReader(ss, path);
        Dataset<Link> edges = networkReader.readEdgeJson();
        ReportHandler reportHandler = new ReportHandler(ss, path, 10);
        Dataset<StepReport> reports = reportHandler.readReportJson();
        TrafficPanel traffic = new TrafficPanel("GeoSparkSim Test");
        traffic.run(edges, reports.collectAsList());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
