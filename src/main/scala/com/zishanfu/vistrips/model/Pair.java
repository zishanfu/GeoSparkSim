package com.zishanfu.vistrips.model;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class Pair {
	private GeoPosition source;
	private GeoPosition dest;
	private LineString route = null;
	
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
	
	public List<Double[]> getRouteGeojson() {
		List<Double[]> res = new ArrayList<>();
		Coordinate[] coordinates = route.getCoordinates();
		for(Coordinate coor: coordinates) {
			Double[] arr = {coor.x, coor.y};
			res.add(arr);
		}
		return res;
	}

	public void setRoute(LineString route2) {
		this.route = route2;
	}

	
}
