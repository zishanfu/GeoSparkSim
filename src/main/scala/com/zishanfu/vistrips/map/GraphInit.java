package com.zishanfu.vistrips.map;

import java.io.Serializable;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.impl.Log4jLoggerAdapter;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.zishanfu.vistrips.JmapConsole;


/**
 * @author zishanfu
 *
 */

public class GraphInit implements Serializable{
	
	private GraphHopper hopper;
	
//	euclidean: 0.044644977654619736
//	harvsine: 21382.936940999596
	private final static Logger LOG = Logger.getLogger(GraphInit.class);

	
	public GraphInit(String osm) {
		this.hopper = new GraphHopper().forServer();
		hopper.setOSMFile(osm);
		hopper.setGraphHopperLocation(osm.substring(0, osm.lastIndexOf("/")) + "/graphhopper/");
		hopper.setEncodingManager(new EncodingManager("car"));
		hopper.setPreciseIndexResolution(1000);
		hopper.importOrLoad();
	}
	
	private PathWrapper requestBest(double latFrom, double lonFrom, double latTo, double lonTo) {
		GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).
				setWeighting("fastest").
				setVehicle("car").
				setLocale(Locale.US);

		GHResponse rsp = hopper.route(req);
		
		if(rsp.hasErrors()) {
			return null;
		}
		
		return rsp.getBest();
	}
	
	
	public PathWrapper routeRequest(Coordinate from, Coordinate to) {
		PathWrapper rsp = requestBest(from.y, from.x, to.y, to.x);
		return rsp == null? null : rsp;
	}
	
	/**
	 * @param latFrom, eg 33.409437
	 * @param lonFrom, eg -111.919828
	 * @param latTo, eg 33.419810
	 * @param lonTo, eg -111.930544
	 * @return String type route waypoints
	 */
	public PathWrapper routeRequest(double latFrom, double lonFrom, double latTo, double lonTo) {
		PathWrapper rsp = requestBest(latFrom, lonFrom, latTo, lonTo);
		return rsp == null? null : rsp;
	}
	
	/**
	 * @param src, source node coordinate
	 * @param des, destination node coordinate
	 * @return best route distance
	 */
	public double routeDistance(double[] src, double[] des) {
		return requestBest(src[0], src[1], des[0], des[1]).getDistance();
	}
	
	public double routeTime(double[] src, double[] des) {
		return requestBest(src[0], src[1], des[0], des[1]).getTime();
	}
	
	public void printInstruction(double[] src, double[] des) {
		String ins = requestBest(src[0], src[1], des[0], des[1]).getInstructions().toString();
		System.out.println("Instruction: " + ins);
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
	 * @param Coordinate node
	 * @return Coordinate closetNode
	 */
	public Coordinate getClosestNode(Coordinate node) {
		GHPoint res = hopper.getLocationIndex().findClosest(node.y, node.x, EdgeFilter.ALL_EDGES).getQueryPoint();
		Coordinate newNode = new Coordinate(res.getLon(), res.getLat());
		return newNode;
	}
	
}
