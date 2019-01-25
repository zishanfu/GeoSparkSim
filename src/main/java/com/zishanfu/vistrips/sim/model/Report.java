package com.zishanfu.vistrips.sim.model;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Coordinate;

public class Report implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1806477329173858692L;
	private int vid;
	private int sid;
	private Coordinate location;
	private double speed;
	private double acceleration;
	public Report(int vid, Coordinate location, double speed, double acceleration) {
		this.vid = vid;
		this.location = location;
		this.speed = speed;
		this.acceleration = acceleration;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public Coordinate getLocation() {
		return location;
	}


	public String toString() {
		return String.format("%s,%s,%s,%s,%s", vid, sid, location, speed, acceleration);
	}
}
