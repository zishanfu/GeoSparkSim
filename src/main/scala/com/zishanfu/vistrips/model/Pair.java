package com.zishanfu.vistrips.model;

import java.io.Serializable;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;


public class Pair implements Serializable{
	private GeoPosition source;
	private GeoPosition dest;
	private LineString route = null;
	private long time;
	private double distance;
	//PointList / LineString
	
	public Pair() {}
	
	public Pair(GeoPosition source, GeoPosition dest, LineString route) {
		this.source = source;
		this.dest = dest;
		this.route = route;
	}
	
	public Pair(double latFrom, double lonFrom, double latTo, double lonTo, LineString route) {
		source = new GeoPosition(latFrom, lonFrom);
		dest = new GeoPosition(latTo, lonTo);
		this.route = route;
	}
	

	public GeoPosition getSourceGeo() {
		return source;
	}
	
	public Coordinate getSourceCoor() {
		return new Coordinate(source.getLongitude(), source.getLatitude());
	}

	public void setSource(GeoPosition source) {
		this.source = source;
	}

	public GeoPosition getDestGeo() {
		return dest;
	}
	
	public Coordinate getDestCoor() {
		return new Coordinate(dest.getLongitude(), dest.getLatitude());
	}

	public void setDest(GeoPosition dest) {
		this.dest = dest;
	}

	public LineString getRoute() {
		return route;
	}
	
	public void setRoute(LineString route) {
		this.route = route;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	
}
