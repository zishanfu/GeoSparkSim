package com.zishanfu.vistrips.sim.model;

import java.io.Serializable;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.vistrips.model.Link;

public class VirtualGPS implements Serializable{
	private Link[] segments;
	private Set<Coordinate> lights;
	private Set<Coordinate> intersects;
	
	public VirtualGPS() {}

	public Link[] getSegments() {
		return segments;
	}

	public void setSegments(Link[] segments) {
		this.segments = segments;
	}

	public Set<Coordinate> getLights() {
		return lights;
	}

	public void setLights(Set<Coordinate> s) {
		this.lights = s;
	}

	public Set<Coordinate> getIntersects() {
		return intersects;
	}

	public void setIntersects(Set<Coordinate> intersects) {
		this.intersects = intersects;
	}
	
	public void addLights(Coordinate coordinate) {
		lights.add(coordinate);
	}
	
	public void addIntersects(Coordinate coordinate) {
		intersects.add(coordinate);
	}
	
}
