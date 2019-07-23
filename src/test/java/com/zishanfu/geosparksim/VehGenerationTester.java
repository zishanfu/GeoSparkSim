package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Generation.CreateVehicles;
import com.zishanfu.geosparksim.Model.Vehicle;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

public class VehGenerationTester extends GeoSparkSimTestBase {

    static JavaSparkContext sc;
    static String resources;
    static FileOps fileOps = new FileOps();
    static Coordinate coor1;
    static Coordinate coor2;
    static int total;
    static String type;

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("EarthdataHDFTest").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        resources = System.getProperty("user.dir") + "/src/test/resources";
        fileOps.createDirectory(resources + "/java-test");

        coor1 = new Coordinate(38.924281, -94.659155);
        coor2 = new Coordinate(38.905553, -94.615693);
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
    public void vehicleGeneration()
    {
        OsmLoader osmLoader = new OsmLoader();
        osmLoader.osm(coor1, coor2);

        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        String osmPath = "datareader.file=" + resources + "/geosparksim/map.osm";

        System.out.println(resources);
        String[] vehParameters = new String[]{"config=" + resources + "/graphhopper/config.properties", osmPath};
        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        try {
            List<Vehicle> vehicleList = createVehicles.multiple(total, type);
            Assert.assertEquals(vehicleList.size(), total);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
