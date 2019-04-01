import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Generation.CreateVehicles;
import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.OSM.OsmParser;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.Tools.FileOps;
import com.zishanfu.geosparksim.osm.VehicleHandler;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class VehicleTest {

    @Test
    public void generation() throws ExecutionException, InterruptedException {
        String resources = System.getProperty("user.dir") + "/src/main/resources";
        String local= resources + "/vistrips";
        new FileOps().createDirectory(local);

        String osmPath = "datareader.file=" + local + "/map.osm";
        String[] vehParameters = new String[]{"config=config.properties", osmPath};

        SparkSession spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("GeoSparkSim")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                .getOrCreate();

        double lat1 = 33.48998;
        double lon1 = -112.10964;
        double lat2 = 33.38827;
        double lon2 = -111.79722;

        Coordinate coor1 = new Coordinate(lat1, lon1);
        Coordinate coor2 = new Coordinate(lat2, lon2);
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(lat1 + maxLen, lon1 - maxLen);
        Coordinate newCoor2 = new Coordinate(lat2 + maxLen, lon2 - maxLen);

        OsmParser osmParser = new OsmParser();
        osmParser.runInLocal(newCoor1, newCoor2, local);

        int total = 50000;
        String type = "DSO";

        long t1 = System.currentTimeMillis();
        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = createVehicles.multiple(total, type);
        VehicleHandler vehicleHandler = new VehicleHandler(spark, local);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) / 1000 + "sec");

//        RoadNetworkReader networkReader = new RoadNetworkReader(spark, local);
//        Dataset<Link> edges = networkReader.readEdgeJson();
//        Dataset<TrafficLight> signals = networkReader.readSignalJson();
//        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();

    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }
}
