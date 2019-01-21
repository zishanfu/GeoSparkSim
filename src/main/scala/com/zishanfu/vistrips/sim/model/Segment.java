package com.zishanfu.vistrips.sim.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;


public class Segment {
	private final static Stroke BASIC_STROKE = new BasicStroke();
	private int laneWidth = 5; //pixel
	
	private final Point pointA, pointB;
	private final Color color;
	private final Stroke stroke;
	private final List<Lane> landList;
	
	/**
	 * Creates a new segment.
	 * 
	 * @param pointA the first point
	 * @param pointB the second point
	 * @param color the color of the segment
	 */
	public Segment(Point pointA, Point pointB, Color color) {
		this(pointA, pointB, color, BASIC_STROKE);
	}
	
	public Segment(Point pointA, Point pointB, Color color, int lanes) {
		int stroke = lanes * laneWidth;
		this.pointA = pointA;
		this.pointB = pointB;
		this.color = color;
		this.stroke = new BasicStroke(stroke);
	}

	/**
	 * Returns the first point of the segment ("A")
	 * 
	 * @return the first point of the segment
	 */
	public Point getPointA() {
		return pointA;
	}

	/**
	 * Returns the second point of the segment ("B")
	 * 
	 * @return the second point of the segment
	 */
	public Point getPointB() {
		return pointB;
	}

	/**
	 * Returns the color of the segment
	 * 
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}
	
	public Stroke getStroke() {
		return stroke;
	}	
}
