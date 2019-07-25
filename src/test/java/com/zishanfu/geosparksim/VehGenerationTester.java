package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Generation.CreateVehicles;
import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.OSM.OsmLoader;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.Tools.FileOps;
import com.zishanfu.geosparksim.osm.VehicleHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.collection.JavaConverters;
import scala.collection.Seq;

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
    static SparkSession ss;

    @BeforeClass

    public static void onceExecutedBeforeAll()
    {
        SparkConf conf = new SparkConf().setAppName("VehGeneration").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
        coor1 = new Coordinate(33.429165, -111.942323);
        coor2 = new Coordinate(33.413572, -111.924442);
        total = 1000;
        type = "DSO";
    }

    @AfterClass
    public static void tearDown()
    {
        fileOps.deleteDirectory(resources + "/samples/map-gh");
        sc.stop();
    }

    @Test
    public void vehicleGeneration()
    {

        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        String osmPath = "datareader.file=" + resources + "/samples/map.osm";

        String[] vehParameters = new String[]{"config=" + resources + "/graphhopper/config.properties", osmPath};
        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        try {
            List<Vehicle> vehicleList = createVehicles.multiple(total, type);
            Assert.assertEquals(vehicleList.size(), total);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
