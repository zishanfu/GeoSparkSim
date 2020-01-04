package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.osm.OsmConverter;
import com.zishanfu.geosparksim.osm.RoadNetwork;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RoadNetworkTester extends GeoSparkSimTestBase {
    static JavaSparkContext sc;
    static SparkSession ss;
    static String resources;

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("RoadNetwork").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
    }

    @AfterClass
    public static void tearDown() {
        sc.stop();
    }

    @Test
    public void roadnetwork() {
        String path = resources + "/samples";
        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(ss, path);
        roadNetwork.nodes().toJavaRDD().take(10).forEach(System.out::println);
        roadNetwork.links().toJavaRDD().take(10).forEach(System.out::println);
        roadNetwork.intersects().toJavaRDD().take(10).forEach(System.out::println);
        roadNetwork.lights().toJavaRDD().take(10).forEach(System.out::println);
    }
}
