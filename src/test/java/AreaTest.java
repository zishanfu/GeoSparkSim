import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Test;

public class AreaTest {
    @Test
    public void squareCompute(){
        //all
        double all_lat1 = 33.48998;
        double all_lon1 = -112.10964;
        double all_lat2 = 33.38827;
        double all_lon2 = -111.79722;

        //33.431673, -111.976903
        //33.327490, -111.895980
        //Tempe
        double tempe_lat1 = 33.431673;
        double tempe_lon1 = -111.976903;
        double tempe_lat2 = 33.327490;
        double tempe_lon2 = -111.895980;

        //Phoenix, Glendale and Period
        //33.801990, -112.260744
        //33.290843, -111.972211
        double phx_lat1 = 33.801990;
        double phx_lon1 = -112.260744;
        double phx_lat2 = 33.290843;
        double phx_lon2 = -111.972211;

        double lat1 = 33.429542;
        double lon1 = -111.947065;
        double lat2 = 33.407316;
        double lon2 = -111.919000;

        double area1 = rectArea(all_lat1, all_lon1, all_lat2, all_lon2);
        double area2 = rectArea(tempe_lat1, tempe_lon1, tempe_lat2, tempe_lon2);
        double area3 = rectArea(phx_lat1, phx_lon1, phx_lat2, phx_lon2);

        double area4 = rectArea(lat1, lon1, lat2, lon2);
        System.out.println("area4: " + area4);

        System.out.println("All: " + area1);
        System.out.println("Tempe: " + area2);
        System.out.println("Phoenix: " + area3);
    }

    // 1 meter to 0.000621371192 mile
    public double rectArea(double lat1, double lon1, double lat2, double lon2) {
        double length = haversine(new Coordinate(lat1, lon2), new Coordinate(lat1, lon1)) * 0.000621371192;
        double height = haversine(new Coordinate(lat2, lon1), new Coordinate(lat2, lon2)) * 0.000621371192;
        return(length * height);
    }

    public double haversine(Coordinate coor1, Coordinate coor2) {
        double latitude1 = Math.toRadians(coor1.x);
        double latitude2 = Math.toRadians(coor2.x);
        double longitude1 = Math.toRadians(coor1.y);
        double longitude2 = Math.toRadians(coor2.y);

        return (1000 * 2 * 6371 * Math.asin(Math.sqrt(
                Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
                        Math.cos(latitude1) * Math.cos(latitude2) *
                                Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2))));
    }
}
