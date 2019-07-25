package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.OSM.OsmLoader;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.Tools.FileOps;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class OsmLoadingTester extends GeoSparkSimTestBase{

    static JavaSparkContext sc;
    static String resources;
    static FileOps fileOps = new FileOps();
    static Coordinate coor1;
    static Coordinate coor2;
    static int total;
    static String type;
    static String path = "";
    //ASU boundary
    //top-left 33.429165, -111.942323
    //bottom-right 33.413572, -111.924442

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("OpenStreetMapData").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        resources = System.getProperty("user.dir") + "/src/test/resources";
        path = resources + "/java-test";
        fileOps.createDirectory(path);

        Coordinate c1 = new Coordinate(33.429165, -111.942323);
        Coordinate c2 = new Coordinate(33.413572, -111.924442);
        double maxLen = new Distance().euclidean(c1.x, c2.x, c1.y, c2.y) / 10;
        coor1 = new Coordinate(c1.x + maxLen, c1.y - maxLen);
        coor2 = new Coordinate(c2.x + maxLen, c2.y - maxLen);

        total = 1000;
        type = "DSO";
    }

    @AfterClass
    public static void tearDown()
    {
        fileOps.deleteDirectory(path);
        sc.stop();
    }

    @Test
    public void testOSMLoading()
    {
        OsmLoader osmLoader = new OsmLoader(coor1, coor2, path);
        osmLoader.osm();
        osmLoader.parquet();
        File osm = new File(path + "/map.osm");
        File node = new File(path + "/node.parquet");
        File way = new File(path + "/way.parquet");
        Assert.assertTrue(osm.exists());
        Assert.assertTrue(node.exists());
        Assert.assertTrue(way.exists());
    }

}

