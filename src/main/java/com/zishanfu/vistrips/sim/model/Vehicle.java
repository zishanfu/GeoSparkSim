package com.zishanfu.vistrips.sim.model;

import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.zishanfu.vistrips.tools.Distance;

public class Vehicle extends LineString{

	private static PrecisionModel precision = new PrecisionModel();
	private double avgSpeed;
	private long time;
	private double distance;
	private Coordinate[] route;
	private VirtualGPS gps = new VirtualGPS();
	public static final Distance distanceTo = new Distance();
	
	//create a looking ahead rectangle and looking back rectangle by current position
	private Set<IDMVehicle> aheadVehicles;
	private VehicleBuffer vBuffer;
	private Polygon self;
	private Coordinate location;
	private Coordinate next;
	private int nextIdx;
	
	//time in seconds
	public Vehicle(Coordinate[] points, int sid, long time, double distance) {
		super(points, precision, sid);
		this.route = points;
		this.avgSpeed = distance / time;
		this.time = time;
		this.location = points[0];
		this.next = points[1];
		this.nextIdx = 1;
		this.vBuffer = new VehicleBuffer(points[0], points[1]);
	}

	public double getDistance() {
		return distance;
	}

	public Coordinate[] getRoute() {
		return route;
	}

	public void setRoute(Coordinate[] route) {
		this.route = route;
	}

	public long getTime() {
		return time;
	}

	
	public int getNextIdx() {
		return nextIdx;
	}

	public Coordinate getNext() {
		return next;
	}

	
	public double getAvgSpeed() {
		return avgSpeed;
	}

	public Coordinate getLocation() {
		return location;
	}

	public boolean setLocation(Coordinate location, int idx) {
		this.location = location;
		if(nextIdx == route.length - 1) {
			return false;
		}else if(location == route[nextIdx] || location.distance(route[nextIdx]) <= 0.000005) {
			this.nextIdx++;
			this.next = route[this.nextIdx];
			this.vBuffer = new VehicleBuffer(location, this.next);
		}else {
			this.nextIdx = idx;
			this.next = route[idx];
			this.vBuffer = new VehicleBuffer(location, this.next);
		}
		return true;
	}

	public Polygon getSelf() {
		return self;
	}

	public void setSelf(Polygon self) {
		this.self = self;
	}

	public VehicleBuffer getvBuffer() {
		return vBuffer;
	}

	public VirtualGPS getGps() {
		return gps;
	}

	public void setGps(VirtualGPS gps) {
		this.gps = gps;
	}

	public Set<IDMVehicle> getAheadVehicles() {
		return aheadVehicles;
	}

	public void setAheadVehicles(Set<IDMVehicle> set) {
		this.aheadVehicles = set;
	}
	
	public Coordinate[] initialShuffle(long time) {
		Coordinate[] coordinates = this.getRoute();
		double cost = 0;
		List<Coordinate> result = new ArrayList<>();
		int i = 0;
		for(;i<coordinates.length-1; i++) {
			cost += distanceTo.harversineMeter(coordinates[i], coordinates[i+1])/avgSpeed;
			if(cost >= time) break;
			result.add(coordinates[i]);
		}
		result.add(coordinates[i]);
		return result.size() >= 2? result.toArray(new Coordinate[result.size()]): coordinates;
	}
	
	public Coordinate[] nextShuffle(long time, Coordinate last) {
		Coordinate[] coordinates = this.getRoute();
		List<Coordinate> result = new ArrayList<>();
		int i = this.nextIdx;
		double cost = distanceTo.harversineMeter(last, coordinates[i])/avgSpeed;
		result.add(last);
		for(;i<coordinates.length - 1; i++) {
			cost += distanceTo.harversineMeter(coordinates[i], coordinates[i+1])/avgSpeed;
			if(cost >= time) break;
			result.add(coordinates[i]);
		}
		result.add(coordinates[i]);
		return result.toArray(new Coordinate[result.size()]);
	}
	
}