package com.zishanfu.vistrips.sim.model;


public class GeoPoint extends Point {
	private final String label;
	
	public GeoPoint(double latitude, double longitude, String label) {
		super(latitude, longitude);
		this.label = label;
	}
	
	public GeoPoint(Point point, String label) {
		this(point.getLatitude(), point.getLongitude(), label);
	}
	
	public String getLabel() {
		return label;
	}
}

