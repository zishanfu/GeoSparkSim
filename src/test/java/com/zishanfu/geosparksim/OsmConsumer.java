package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.OSM.OsmParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OsmConsumer extends GeoSparkSimTestBase {

    public static JavaSparkContext sc;

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("EarthdataHDFTest").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
    }

    @AfterClass
    public static void tearDown()
    {
        sc.stop();
    }


    @Test
    public void testOSMConsuming()
    {
        Coordinate coor1 = new Coordinate(38.924281, -94.659155);
        Coordinate coor2 = new Coordinate(38.905553, -94.615693);
        //String hdfs = "hdfs://localhost:9000/usr/geosparksim";
        String hdfs = "/Users/SammiFu/Downloads/geosparksimoo";
        OsmParser osmParser = new OsmParser();
        osmParser.runInHDFS(coor1, coor2, hdfs);
    }
}
