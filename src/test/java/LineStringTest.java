
import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Model.Link;
import com.zishanfu.geosparksim.model.SegmentNode;
import org.junit.jupiter.api.Test;

public class LineStringTest {


    //Link(long id, SegmentNode head, SegmentNode tail, double distance, int speed, int driveDirection, int lanes, double angle, String laneArray, Coordinate[] path)
    //SegmentNode(id: Long, coordinate: Coordinate, signal: Boolean, intersect: Boolean)

    //330683940, 33.4148172, -111.9262878
    //2300341794, 33.4149285, -111.9262851
    @Test
    public void linkTest(){
        SegmentNode head = new SegmentNode(330683940L, new Coordinate(33.4148172, -111.9262878), true, false);
        SegmentNode tail = new SegmentNode(2300341794L, new Coordinate(33.4149285, -111.9262851), false, false);
        Coordinate[] coordinates = new Coordinate[]{head.coordinate(), tail.coordinate()};
        Link link = new Link(436942381L, head, tail, 8.0, 40, 1, 4, 1.0, "", coordinates);
        System.out.println(link.getEnvelope());
        System.out.println(link.getEnvelopeInternal());
    }

    public void vehicleTest(){

    }
}
