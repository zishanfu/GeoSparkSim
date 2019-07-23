package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.OSM.OsmLoader;
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
    //ASU boundary
    //top-left 33.429165, -111.942323
    //bottom-right 33.413572, -111.924442

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("EarthdataHDFTest").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        resources = System.getProperty("user.dir") + "/src/test/resources";
        fileOps.createDirectory(resources + "/java-test");

        coor1 = new Coordinate(33.429165, -111.942323);
        coor2 = new Coordinate(33.413572, -111.924442);
        total = 1000;
        type = "DSO";
    }

    @AfterClass
    public static void tearDown()
    {
        fileOps.deleteDirectory(resources + "/java-test");
        fileOps.deleteDirectory(resources + "/geosparksim");
        sc.stop();
    }

    @Test
    public void testOSMLoading()
    {
        //String hdfs = "hdfs://localhost:9000/geosparksim";
        OsmLoader osmLoader = new OsmLoader();
        osmLoader.osm(coor1, coor2);
        osmLoader.parquet(coor1, coor2, resources + "/java-test");
        File osm = new File(resources + "/geosparksim" + "/map.osm");
        File node = new File(resources + "/java-test" + "/node.parquet");
        File way = new File(resources + "/java-test" + "/way.parquet");
        Assert.assertTrue(osm.exists());
        Assert.assertTrue(node.exists());
        Assert.assertTrue(way.exists());
    }

}

