package com.zishanfu.vistrips.model;

import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.PointList;

public class Pair {
	private GeoPosition source;
	private GeoPosition dest;
	private PointList route = null;
	
	public Pair() {}
	
	public Pair(GeoPosition source, GeoPosition dest, PointList route) {
		this.source = source;
		this.dest = dest;
		this.route = route;
	}
	
	public Pair(double latFrom, double lonFrom, double latTo, double lonTo, PointList route) {
		source = new GeoPosition(latFrom, lonFrom);
		dest = new GeoPosition(latTo, lonTo);
		this.route = route;
	}

	public GeoPosition getSource() {
		return source;
	}

	public void setSource(GeoPosition source) {
		this.source = source;
	}

	public GeoPosition getDest() {
		return dest;
	}

	public void setDest(GeoPosition dest) {
		this.dest = dest;
	}

	public PointList getRoute() {
		return route;
	}

	public void setRoute(PointList route) {
		this.route = route;
	}

	
}
