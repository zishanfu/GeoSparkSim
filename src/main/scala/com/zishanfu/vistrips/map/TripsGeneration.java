package com.zishanfu.vistrips.map;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.LineString;
import com.zishanfu.vistrips.model.Pair;

/**
 * @author zishanfu
 *
 */
public class TripsGeneration{
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
	private OsmGraph graph;
	//private Distance dist;
	
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
	
	
	public TripsGeneration(GeoPosition topleft, GeoPosition bottomright, OsmGraph graph, double maxLen) {
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
	
	private void updateLongestTrip(int len) {
		if(len > longestTrip) {
			longestTrip = len;
		}
	}
	
	
	/**
	 * @param type The type for generate data, data-space oriented approach(DSO), 
	 * region-based approach(RB), network-based approach(NB)
	 * @return a Pair of node
	 */
	public Pair computeAPair(String type){
		Pair p = new Pair();
		double routeLen = computeLengthOfRoute();
		if(type.contains(DSO)) {
			GeoPosition src = randomNode();
			p.setSource(src);
			p.setDest(computeDestDSO(src, routeLen));
		}else if(type.contains(NB)) {
			//GeoPosition src = gi.getCoorById(randomIdx());
			GeoPosition src = computeSourceNB();
			p.setSource(src);
			p.setDest(computeDestinationNB(src, routeLen));
		}else if(type.contains(RB)) {
			
		}	
		LineString route = graph.routeRequest(p.getSource().getLatitude(),
						p.getSource().getLongitude(),
						p.getDest().getLatitude(),
						p.getDest().getLongitude());
		System.out.println(route);
		if(route == null) 
			return null;
		updateLongestTrip(route.getNumPoints());
		p.setRoute(route);
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
	

}
