package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.osm.OsmConverter;
import com.zishanfu.geosparksim.osm.RoadNetwork;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoadNetworkTester extends GeoSparkSimTestBase {
    private JavaSparkContext sc;
    private SparkSession ss;
    private String resources;

    @Before
    public void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("RoadNetwork").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        String path =
                RoadNetworkTester.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath();
        int idx = path.indexOf("/target");
        resources = path.substring(0, idx) + "/src/test/resources";
    }

    @After
    public void tearDown() {
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
