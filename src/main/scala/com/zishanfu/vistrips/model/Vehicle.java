package com.zishanfu.vistrips.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Vehicle extends LineString{
	
	private Coordinate source;
	private Coordinate destination;
	private long time;
	private double distance;
	
	public Vehicle(Coordinate[] points, PrecisionModel precisionModel, int SRID) {
		super(points, precisionModel, SRID);
		// TODO Auto-generated constructor stub
	}
	
	//src, dest, distance, time in section
	public void apply(Coordinate source, Coordinate destination, double distance, long time) {
		this.source = source;
		this.destination = destination;
		this.distance = distance;
		this.time = time;
	}
	

}
