package com.zishanfu.vistrips.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.spark.sql.SparkSession;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.PrecisionModel.Type;
import com.zishanfu.vistrips.model.Pair;
import com.zishanfu.vistrips.tools.Distance;

/**
 * @author zishanfu
 *
 */
public class TripsGeneration implements Serializable{
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	private Random rand = new Random();
	private long totalNodes;
	private double minLen = 0.004; //1.9km
	private double maxLen;
	//private double maxLenharv;
	private int longestTrip = 0;
	//private OsmGraph graph;
	private GraphInit graph;
	//private Distance dist;
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	private double longestTripTime = 0;
	private SparkSession sparkSession;
	private PrecisionModel precision = new PrecisionModel();
	
	
	//1:479km = euclidean: harvsine
	//Car 4.5m
	//safe distance 2m
	//11m
	//1/x = 479000/11
	//x = 0.000023
	
	/**
	 * data-space oriented approach(DSO)
	 */
	private String DSO = "DSO";
	/**
	 * region-based approach(RB)
	 */
	private String RB = "RB";
	/**
	 * network-based approach(NB)
	 */
	private String NB = "NB";
	
	public TripsGeneration(GeoPosition topleft, GeoPosition bottomright, GraphInit graph, double maxLen) {
	//public TripsGeneration(GeoPosition topleft, GeoPosition bottomright, OsmGraph graph, double maxLen) {
		this.minLat = Math.min(topleft.getLatitude(), bottomright.getLatitude());
		this.maxLat = Math.max(topleft.getLatitude(), bottomright.getLatitude());
		this.minLon = Math.min(topleft.getLongitude(), bottomright.getLongitude());
		this.maxLon = Math.max(topleft.getLongitude(), bottomright.getLongitude());
		this.graph = graph;
		this.totalNodes = graph.getTotalNodes();
		this.maxLen = maxLen;
		//this.dist = new Distance();
		//this.maxLenharv = dist.haversine(minLat, minLon, maxLat, maxLon);
	}
	

	private double randomRange(double low, double high) {
		return rand.nextDouble()*(high - low) + low;
	}
	
	private GeoPosition randomNode() {
		GeoPosition node = new GeoPosition(randomRange(minLat, maxLat), randomRange(minLon, maxLon));
		return node;
	}
	
	private double computeLengthOfRoute() {
		double r = Math.abs(rand.nextGaussian());
		double lenDiff = maxLen - minLen;
		double len = lenDiff <= 0? minLen: (r * (maxLen - minLen) + minLen);
		return len;
	}
	
	
	private GeoPosition computeDestDSO(GeoPosition src, double len) {
		double angle = ThreadLocalRandom.current().nextDouble(360);
		double lat = src.getLatitude() + len * Math.cos(angle);
		double lon = src.getLongitude() + len * Math.sin(angle);
		return new GeoPosition(lat, lon);
	}
	
//	private GeoPosition computeDestDSO(GeoPosition src, double len) {
//		double angle = ThreadLocalRandom.current().nextDouble(360);
//		double lat1 = Math.toRadians(src.getLatitude());
//		double lon1 = Math.toRadians(src.getLongitude());
//		
//		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(len/gi.RADIUS_OF_EARTH) +
//                Math.cos(lat1)*Math.sin(len/gi.RADIUS_OF_EARTH)*Math.cos(angle) );
//		double lon2 = lon1 + Math.atan2(Math.sin(angle)*Math.sin(len/gi.RADIUS_OF_EARTH)*Math.cos(lat1),
//                     Math.cos(len/gi.RADIUS_OF_EARTH)-Math.sin(lat1)*Math.sin(lat2));
//		
//		return new GeoPosition(lat2, lon2);
//	}
	
	private GeoPosition computeSourceNB() {
		GeoPosition src = randomNode();
		return graph.getClosestNode(src);
	}
	
	private GeoPosition computeDestinationNB(GeoPosition src, double len) {
		GeoPosition dest = computeDestDSO(src, len);
		return graph.getClosestNode(dest);
	}
	
	private void updateLongestTrip(int len, double time) {
		if(len > longestTrip) {
			longestTrip = len;
		}
		if(time > longestTripTime) {
			longestTripTime = time;
		}
	}

	
	public void generateNumTripBox(Coordinate coor1, Coordinate coor2, int num) {
		
	}
	
	
	/**
	 * @param type The type for generate data, data-space oriented approach(DSO), 
	 * region-based approach(RB), network-based approach(NB)
	 * @return a Pair of node
	 */
	public Pair computeAPair(String type){
		//Pair p = new Pair();
		double routeLen = computeLengthOfRoute();
		GeoPosition src = null;
		GeoPosition dest = null;
		if(type.contains(DSO)) {
//			GeoPosition src = randomNode();
//			p.setSource(src);
//			p.setDest(computeDestDSO(src, routeLen));
			src = randomNode();
			dest = computeDestDSO(src, routeLen);
		}else if(type.contains(NB)) {
//			GeoPosition src = computeSourceNB();
//			p.setSource(src);
//			p.setDest(computeDestinationNB(src, routeLen));
			src = computeSourceNB();
			dest = computeDestinationNB(src, routeLen);
		}else if(type.contains(RB)) {
			
		}	

//		Route route = graph.fatestRouteRequest(p.getSource().getLatitude(),
//						p.getSource().getLongitude(),
//						p.getDest().getLatitude(),
//						p.getDest().getLongitude());
//		if(route == null) 
//			return null;
//		LineString legs = route.getLegsLineString();
//		updateLongestTrip(legs.getNumPoints());
//		p.setRoute(legs);
		PathWrapper path = graph.routeRequest(src, dest);
		if(path == null) {
			return computeAPair(type);
		}
		PointList route = path.getPoints();
		if(route == null || route.size() <= 1) 
			return computeAPair(type);
		updateLongestTrip(route.size(), path.getTime()/1000);
		LineString lsRoute = PointList2LineString(route);
//		LineString routeInSec = routeInterpolate(lsRoute, path.getTime()/1000, path.getDistance());
		//p.setRoute(routeInSec);
		Pair p = new Pair(lsRoute.getCoordinates(), precision, 1);
		p.setRoute(lsRoute);
		p.setDistance(path.getDistance());
		p.setTime(path.getTime()/1000);
		return p;
	}
	
	
	/**
	 * @param num The total number of simulation objects
	 * @param type The type for generate data, data-space oriented approach(DSO), 
	 * region-based approach(RB), network-based approach(NB)
	 * @return Pair[] Pairs of node
	 */
	public Pair[] computePairs(int num, String type){
		Pair[] pairs = new Pair[num];
		for(int i = 0; i<num; i++) {
			Pair pair = computeAPair(type);
			if(pair != null) {
				pairs[i] = computeAPair(type);
			}
		}
		return pairs;
	}


	public int getLongestTrip() {
		return longestTrip;
	}
	
	
	public double getLongestTripTime() {
		return longestTripTime;
	}
	
	
	private LineString PointList2LineString(PointList pl) {
		int len = pl.getSize();
		//lon, lat
		Coordinate[] coordinates = new Coordinate[len];
		for(int i = 0; i<len; i++) {
			coordinates[i] = new Coordinate(pl.getLon(i), pl.getLat(i));
		}
		return geometryFactory.createLineString(coordinates);
	}
	
	
}
