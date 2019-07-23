package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.Tools.FileOps;
import com.zishanfu.geosparksim.osm.OsmConverter;
import com.zishanfu.geosparksim.osm.RoadNetwork;
import com.zishanfu.geosparksim.osm.RoadNetworkWriter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RoadNetworkTester extends GeoSparkSimTestBase{
    static JavaSparkContext sc;
    static SparkSession ss;
    static String resources;
    static FileOps fileOps = new FileOps();

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("EarthdataHDFTest").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
        fileOps.createDirectory(resources + "/java-test");
    }

    @AfterClass
    public static void tearDown()
    {
        //fileOps.deleteDirectory(resources + "/java-test");
        //fileOps.deleteDirectory(resources + "/geosparksim");
        sc.stop();
    }

    @Test
    public void roadnetwork()
    {
        String path = resources + "/samples";
        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(ss, path);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(ss, roadNetwork, resources + "/java-test");
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();
    }
}
