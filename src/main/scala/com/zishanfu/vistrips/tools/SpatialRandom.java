package com.zishanfu.vistrips.tools;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.graphhopper.PathWrapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.vistrips.osm.GraphInit;

public class SpatialRandom{

	private Random rand = new Random();
	private double minLon;
	private double minLat;
	private double maxLon;
	private double maxLat;
	private double minLen = 0.004; //1.9km
	private double maxLen;
	private GraphInit graph;
	
	/**
	 * @param double minLon
	 * @param double minLat
	 * @param double maxLon
	 * @param double maxLat
	 * @param double maxLen
	 * @param GraphInit gi
	 */
	public SpatialRandom(double minLon, double minLat, double maxLon, double maxLat, 
			double maxLen, GraphInit gi) {
		this.minLon = minLon;
		this.minLat = minLat;
		this.maxLon = maxLon;
		this.maxLat = maxLat;
		this.maxLen = maxLen;
		this.graph = gi;
	}
	
	public PathWrapper getPath(Coordinate src, Coordinate dest) {
		return graph.routeRequest(src, dest);
	}

	
	public double randomRange(double low, double high) {
		return rand.nextDouble()*(high - low) + low;
	}
	
	public Coordinate spatialRandomNode() {
		Coordinate node = new Coordinate(randomRange(minLon, maxLon), randomRange(minLat, maxLat));
		return node;
	}
	
	public double spatialRandomLen() {
		double r = Math.abs(rand.nextGaussian());
		double lenDiff = maxLen - minLen;
		double len = lenDiff <= 0? minLen: (r * (maxLen - minLen) + minLen);
		return len;
	}
	
	public Coordinate computeDestDSO(Coordinate src, double len) {
		double angle = ThreadLocalRandom.current().nextDouble(360);
		double lon = src.x + len * Math.sin(angle);
		double lat = src.y + len * Math.cos(angle);
		return new Coordinate(lon, lat);
	}
	
	public Coordinate computeSourceNB() {
		Coordinate src = spatialRandomNode();
		return graph.getClosestNode(src);
	}
	
	public Coordinate computeDestinationNB(Coordinate src, double len) {
		Coordinate dest = computeDestDSO(src, len);
		return graph.getClosestNode(dest);
	}

}
