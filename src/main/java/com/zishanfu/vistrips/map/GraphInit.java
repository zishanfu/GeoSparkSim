package com.zishanfu.vistrips.map;

import java.util.Locale;

import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;


/**
 * @author zishanfu
 *
 */

public class GraphInit {
	
	final private String osmLoc = "/home/zishanfu/Downloads/datasets/";
	private GraphHopper hopper;
	public final double RADIUS_OF_EARTH = 6371;
//	euclidean: 0.044644977654619736
//	harvsine: 21382.936940999596

	
	//  /home/zishanfu/eclipse-workspace/VisTrips_v1/
	
	public GraphInit() {
		this.hopper = new GraphHopper().forServer();
		hopper.setOSMFile(osmLoc + "arizona-latest.osm.pbf");
		hopper.setGraphHopperLocation(osmLoc + "graphhopper/");
		hopper.setEncodingManager(new EncodingManager("car"));
		hopper.setPreciseIndexResolution(1000);
		hopper.importOrLoad();
		
	}
	
	private GHResponse request(double latFrom, double lonFrom, double latTo, double lonTo) {
		GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).
				setWeighting("fastest").
				setVehicle("car").
				setLocale(Locale.US);

		GHResponse rsp = hopper.route(req);
		
		if(rsp.hasErrors()) {
			System.out.println(latFrom + "," + lonFrom + "," + latTo + "," + lonTo);
			System.out.println("rsp error:" + rsp.getErrors());
			return null;
		}
		
		return rsp;
	}

	/**
	 * @param latFrom, eg 33.409437
	 * @param lonFrom, eg -111.919828
	 * @param latTo, eg 33.419810
	 * @param lonTo, eg -111.930544
	 * @return String type route waypoints
	 */
	public PointList routeRequest(double latFrom, double lonFrom, double latTo, double lonTo) {
		GHResponse rsp = request(latFrom, lonFrom, latTo, lonTo);
		return rsp == null? null : rsp.getBest().getPoints();
	}
	
//	public double[] routeLegsRequest(double latFrom, double lonFrom, double latTo, double lonTo) {
//		PointList route = routeRequest(latFrom , lonFrom, latTo, lonTo);
//		double[] routeLegs = new double[route.size()];
//		for(int i = 0; i<route.size(); i++) {
//			route.get
//		}
//	}
//	
	
	/**
	 * @param src, source node coordinate
	 * @param des, destination node coordinate
	 * @return best route distance
	 */
	public double routeDistCompute(double[] src, double[] des) {
		return request(src[0], src[1], des[0], des[1]).getBest().getDistance();
	}

	
	/**
	 * @return long totalNodes in Graph
	 */
	public long getTotalNodes() {
		return this.hopper.getGraphHopperStorage().getBaseGraph().getNodes();
	}
	
	
	/**
	 * @param long id
	 * @return double[] coor {lat, lon}
	 */
	public GeoPosition getCoorById(long id) {
		double lat = hopper.getGraphHopperStorage().getNodeAccess().getLat((int) id);
		double lon = hopper.getGraphHopperStorage().getNodeAccess().getLon((int) id);
		GeoPosition node = new GeoPosition(lat, lon);	
		return node;
	}
	
	
	/**
	 * @param lat
	 * @param lon
	 * @return double[] coor {lat, lon}
	 */
	public GeoPosition getClosestNode(GeoPosition node) {
		GHPoint res = hopper.getLocationIndex().findClosest(node.getLatitude(), node.getLongitude(), EdgeFilter.ALL_EDGES).getQueryPoint();
		GeoPosition newNode = new GeoPosition(res.getLat(), res.getLon());
		return newNode;
	}
	

	private double euclidean(double lat1, double lon1, double lat2, double lon2) {
		double deltaX = Math.abs(lat1 - lat2);
		double deltaY = Math.abs(lon1 - lon2);
		return (double) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}
	
    
    /**
    * Calculates the distance between two points by haversine formula
    *
    * @param latitude1  Latitude of first coordinate
    * @param longitude1 Longitude of first coordinate
    * @param latitude2  Latitude of second coordinate
    * @param longitude2 Longitude of second coordinate
    * @return           Distance between the two points, in m
    */
    public double haversineDist(double latitude1, double longitude1, double latitude2, double longitude2) {
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude1 = Math.toRadians(longitude1);
        longitude2 = Math.toRadians(longitude2);
        
        return (1000 * 2 * RADIUS_OF_EARTH * Math.asin(Math.sqrt(
          Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
          Math.cos(latitude1) * Math.cos(latitude2) *
          Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2)
        )));
    }
	
	
	/**
	 * Calculates the distance between two points by euclidean distance
	 * 
	 * @param A, a node of coordinate
	 * @param B, a node of coordinate
	 * @return the distance between A and B
	 */
	public double euclideanDist(double[] A, double[] B) {
		return euclidean(A[0], A[1], B[0], B[1]);
	}
	
	
	/**
	 * Calculates the distance between two points by euclidean distance
	 * 
	 * @param lat1
	 * @param lat2
	 * @param lon1
	 * @param lon2
	 * @return
	 */
	public double euclideanDist(double lat1, double lat2, double lon1, double lon2) {
		return euclidean(lat1, lon1, lat2, lon2);
	}

	
}
