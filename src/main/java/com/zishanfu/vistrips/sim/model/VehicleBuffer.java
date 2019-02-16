package com.zishanfu.vistrips.sim.model;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

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
	private Polygon head;
	private Polygon back;
	
	public VehicleBuffer(Coordinate current, Coordinate next) {
		this.location = current;
		this.headAngle = Angle.angle(current, next);
		this.backAngle = Angle.angle(next, current);
		this.head = headBuffer();
		this.back = backBuffer();
		this.next = next;
	}
	
	public Polygon getSelf() {
		double selfLength = location.distance(next);
		double dx = Math.cos(headAngle)*selfLength;
		double dy = Math.sin(headAngle)*selfLength;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate head = new Coordinate(x, y);
		Coordinate[] coordinates = new Coordinate[5];
		if(dx < dy) {
			coordinates[0] = new Coordinate(head.x + selfWidth, head.y);
			coordinates[1] = new Coordinate(head.x - selfWidth, head.y);
			coordinates[2] = new Coordinate(location.x + selfWidth, location.y);
			coordinates[3] = new Coordinate(location.x - selfWidth, location.y);
		}else {
			coordinates[0] = new Coordinate(head.x, head.y + selfWidth);
			coordinates[1] = new Coordinate(head.x, head.y - selfWidth);
			coordinates[2] = new Coordinate(location.x, location.y + selfWidth);
			coordinates[3] = new Coordinate(location.x, location.y - selfWidth);
		}
		coordinates[4] = coordinates[0];
		return gf.createPolygon(coordinates);
	}
	

	public Polygon getHead() {
		return head;
	}

	public Polygon getBack() {
		return back;
	}
	
	public double getHeadAngle() {
		return headAngle;
	}
	
	public double getBackAngle() {
		return backAngle;
	}


	private Polygon headBuffer() {
		double dx = Math.cos(headAngle)*length;
		double dy = Math.sin(headAngle)*length;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate head = new Coordinate(x, y);
		Coordinate[] coordinates = new Coordinate[5];
		if(dx < dy) {
			coordinates[0] = new Coordinate(head.x + headWidth, head.y);
			coordinates[1] = new Coordinate(head.x - headWidth, head.y);
			coordinates[2] = new Coordinate(location.x + headWidth, location.y);
			coordinates[3] = new Coordinate(location.x - headWidth, location.y);
		}else {
			coordinates[0] = new Coordinate(head.x, head.y + headWidth);
			coordinates[1] = new Coordinate(head.x, head.y - headWidth);
			coordinates[2] = new Coordinate(location.x, location.y + headWidth);
			coordinates[3] = new Coordinate(location.x, location.y - headWidth);
		}
		coordinates[4] = coordinates[0];
		return gf.createPolygon(coordinates);
	}
	
	private Polygon backBuffer() {
		double dx = Math.cos(backAngle)*length;
		double dy = Math.sin(backAngle)*length;
		double x = dx + location.x;
		double y = dy + location.y;
		Coordinate back = new Coordinate(x, y);
		Coordinate[] coordinates = new Coordinate[5];
		if(dx > dy) {
			coordinates[0] = new Coordinate(back.x + backWidth, back.y);
			coordinates[1] = new Coordinate(back.x - backWidth, back.y);
			coordinates[2] = new Coordinate(location.x + backWidth, location.y);
			coordinates[3] = new Coordinate(location.x - backWidth, location.y);
		}else {
			coordinates[0] = new Coordinate(back.x, back.y + backWidth);
			coordinates[1] = new Coordinate(back.x, back.y - backWidth);
			coordinates[2] = new Coordinate(location.x, location.y + backWidth);
			coordinates[3] = new Coordinate(location.x, location.y - backWidth);
		}
		coordinates[4] = coordinates[0];
		return gf.createPolygon(coordinates);
	}
}