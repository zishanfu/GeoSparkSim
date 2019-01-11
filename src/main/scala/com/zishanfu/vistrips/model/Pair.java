package com.zishanfu.vistrips.model;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.LineString;


public class Pair{
	private GeoPosition source;
	private GeoPosition dest;
	private LineString route = null;
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

	public LineString getRoute() {
		return route;
	}
	
	public void setRoute(LineString route) {
		this.route = route;
	}

	
}
