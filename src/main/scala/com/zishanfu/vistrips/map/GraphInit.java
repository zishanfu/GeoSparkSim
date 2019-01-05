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

public class GraphInit{
	
	//final private String osmLoc = System.getProperty("user.home") + "/Downloads/datasets/";
	private GraphHopper hopper;
	
//	euclidean: 0.044644977654619736
//	harvsine: 21382.936940999596

	
	//  /home/zishanfu/eclipse-workspace/VisTrips_v1/
	
	public GraphInit(String osm) {
		this.hopper = new GraphHopper().forServer();
		hopper.setOSMFile(osm);
		hopper.setGraphHopperLocation(osm.substring(0, osm.lastIndexOf("/")) + "/graphhopper/");
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
	
}
