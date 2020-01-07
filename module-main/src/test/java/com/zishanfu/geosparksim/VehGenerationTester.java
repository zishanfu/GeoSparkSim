package com.zishanfu.geosparksim;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.generation.CreateVehicles;
import com.zishanfu.geosparksim.model.Vehicle;
import com.zishanfu.geosparksim.tools.Distance;
import com.zishanfu.geosparksim.tools.FileOps;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.*;

public class VehGenerationTester extends GeoSparkSimTestBase {

    private JavaSparkContext sc;
    private String resources;
    private FileOps fileOps = new FileOps();
    private Coordinate coor1;
    private Coordinate coor2;
    private int total;
    private String type;
    private SparkSession ss;

    @Before
    public void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("VehGeneration").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        String path =
                VehGenerationTester.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath();
        int idx = path.indexOf("/target");
        resources = path.substring(0, idx) + "/src/test/resources";
        coor1 = new Coordinate(33.429165, -111.942323);
        coor2 = new Coordinate(33.413572, -111.924442);
        total = 1000;
        type = "DSO";
    }

    @After
    public void tearDown() {
        fileOps.deleteDirectory(resources + "/samples/map-gh");
        sc.stop();
    }

    @Test
    public void vehicleGeneration() throws ExecutionException, InterruptedException {
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        String osmPath = "datareader.file=" + resources + "/samples/map.osm";

        String[] vehParameters =
                new String[] {"config=" + resources + "/graphhopper/config.properties", osmPath};
        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = createVehicles.multiple(total, type);
        Assert.assertEquals(vehicleList.size(), total);
    }
}
