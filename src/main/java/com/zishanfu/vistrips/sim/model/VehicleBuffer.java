package com.zishanfu.vistrips.sim.model;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import scala.Serializable;

public class VehicleBuffer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 702405736549341549L;
	private Coordinate location;
	private Coordinate next;
	private double headAngle;
	private double backAngle;
	private final double length = 0.00005; //25m
	private final double headWidth = 0.00000772;
	private final double backWidth = 0.00002317;
	private final double selfWidth = 0.0000021;
	private GeometryFactory gf = new GeometryFactory();
	private double[] head;
	private double[] back; //left, right, top, bottom
	
	public VehicleBuffer(Coordinate current, Coordinate next) {
		this.location = current;
		this.headAngle = Angle.angle(current, next);
		this.backAngle = Angle.angle(next, current);
		this.head = headBuffer();
		this.back = backBuffer();
		this.next = next;
	}
	
	public double[] getSelf() {
		double selfLength = location.distance(next);
		double dx = Math.cos(headAngle)*selfLength;
		double dy = Math.sin(headAngle)*selfLength;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate head = new Coordinate(x, y);
		double[] results = new double[4];
		if(dx < dy) {
//			Coordinate(head.x + selfWidth, head.y);
//			Coordinate(head.x - selfWidth, head.y);
//			Coordinate(location.x + selfWidth, location.y);
//			Coordinate(location.x - selfWidth, location.y);
			results[0] = Math.min(head.x - selfWidth, location.x - selfWidth);
			results[1] = Math.max(head.x + selfWidth, location.x + selfWidth);
			results[2] = Math.max(head.y, location.y);
			results[3] = Math.min(head.y, location.y);
		}else {
//			Coordinate(head.x, head.y + selfWidth);
//			Coordinate(head.x, head.y - selfWidth);
//			Coordinate(location.x, location.y + selfWidth);
//			Coordinate(location.x, location.y - selfWidth);
			results[0] = Math.min(head.x, location.x);
			results[1] = Math.max(head.x, location.x);
			results[2] = Math.max(head.y - selfWidth, location.y - selfWidth);
			results[3] = Math.min(head.y + selfWidth, location.y + selfWidth);
		}
		return results;
	}
	

	public double[] getHead() {
		return head;
	}

	public double[] getBack() {
		return back;
	}
	
	public double getHeadAngle() {
		return headAngle;
	}
	
	public double getBackAngle() {
		return backAngle;
	}


	private double[] headBuffer() {
		double dx = Math.cos(headAngle)*length;
		double dy = Math.sin(headAngle)*length;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate head = new Coordinate(x, y);
		double[] results = new double[4];
		if(dx < dy) {
//			Coordinate(head.x + headWidth, head.y);
//			Coordinate(head.x - headWidth, head.y);
//			Coordinate(location.x + headWidth, location.y);
//			Coordinate(location.x - headWidth, location.y);
			results[0] = Math.min(head.x - headWidth, location.x - headWidth);
			results[1] = Math.max(head.x + headWidth, location.x + headWidth);
			results[2] = Math.max(head.y, location.y);
			results[3] = Math.min(head.y, location.y);
		}else {
//			Coordinate(head.x, head.y + headWidth);
//			Coordinate(head.x, head.y - headWidth);
//			Coordinate(location.x, location.y + headWidth);
//			Coordinate(location.x, location.y - headWidth);
			results[0] = Math.min(head.x, location.x);
			results[1] = Math.max(head.x, location.x);
			results[2] = Math.max(head.y - headWidth, location.y - headWidth);
			results[3] = Math.min(head.y + headWidth, location.y + headWidth);
		}
		return results;
	}
	
	private double[] backBuffer() {
		double dx = Math.cos(backAngle)*length;
		double dy = Math.sin(backAngle)*length;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate back = new Coordinate(x, y);
		double[] results = new double[4];
		if(dx > dy) {
//			Coordinate(back.x + backWidth, back.y);
//			Coordinate(back.x - backWidth, back.y);
//			Coordinate(location.x + backWidth, location.y);
//			Coordinate(location.x - backWidth, location.y);
			results[0] = Math.min(back.x - backWidth, location.x - backWidth);
			results[1] = Math.max(back.x + backWidth, location.x + backWidth);
			results[2] = Math.max(back.y, location.y);
			results[3] = Math.min(back.y, location.y);
		}else {
//			Coordinate(back.x, back.y + backWidth);
//			Coordinate(back.x, back.y - backWidth);
//			Coordinate(location.x, location.y + backWidth);
//			Coordinate(location.x, location.y - backWidth);
			results[0] = Math.min(back.x, location.x);
			results[1] = Math.max(back.x, location.x);
			results[2] = Math.max(back.y - backWidth, location.y - backWidth);
			results[3] = Math.min(back.y + backWidth, location.y + backWidth);
		}
		return results;
	}
}
