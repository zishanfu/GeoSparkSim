import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.OSM.OsmParser;
import com.zishanfu.geosparksim.Tools.Distance;
import com.zishanfu.geosparksim.Tools.FileOps;
import com.zishanfu.geosparksim.osm.OsmConverter;
import com.zishanfu.geosparksim.osm.RoadNetwork;
import com.zishanfu.geosparksim.osm.RoadNetworkWriter;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

public class GraphTest {
    @Test
    public void graphcompute(){
        //all
//        double lat1 = 33.48998;
//        double lon1 = -112.10964;
//        double lat2 = 33.38827;
//        double lon2 = -111.79722;

        //33.431673, -111.976903
        //33.327490, -111.895980
        //Tempe
//        double lat1 = 33.431673;
//        double lon1 = -111.976903;
//        double lat2 = 33.327490;
//        double lon2 = -111.895980;

        //Phoenix, Glendale and Period
        //33.801990, -112.260744
        //33.290843, -111.972211
        double lat1 = 33.801990;
        double lon1 = -112.260744;
        double lat2 = 33.290843;
        double lon2 = -111.972211;

        SparkSession spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("GeoSparkSim")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                .getOrCreate();

        String resources = System.getProperty("user.dir") + "/src/main/resources";
        String local= resources + "/vistrips";
        new FileOps().createDirectory(local);


        Coordinate coor1 = new Coordinate(lat1, lon1);
        Coordinate coor2 = new Coordinate(lat2, lon2);
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(lat1 + maxLen, lon1 - maxLen);
        Coordinate newCoor2 = new Coordinate(lat2 + maxLen, lon2 - maxLen);


        OsmParser osmParser = new OsmParser();
        osmParser.runInLocal(newCoor1, newCoor2, local);

        long t1 = System.currentTimeMillis();
        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, local);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, local);
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();
        long t2 = System.currentTimeMillis();
        System.out.println((t2 - t1) / 1000 + " sec");
    }
}
