package com.zishanfu.vistrips.sim.model;

import scala.Serializable;

public class VehicleRectangle implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3910621122293967437L;
	private double[] rectangle;
	private IDMVehicle vehicle;
	
	public VehicleRectangle(double[] rectangle) {
		this.rectangle = rectangle;
	}
	public double[] getRectangle() {
		return rectangle;
	}
	public void setRectangle(double[] rectangle) {
		this.rectangle = rectangle;
	}
	public IDMVehicle getVehicle() {
		return vehicle;
	}
	public void setVehicle(IDMVehicle vehicle) {
		this.vehicle = vehicle;
	}
	
}
