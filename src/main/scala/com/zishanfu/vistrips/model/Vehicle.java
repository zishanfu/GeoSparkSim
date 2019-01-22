package com.zishanfu.vistrips.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Vehicle extends LineString{
	
	private Coordinate source;
	private Coordinate destination;
	private long time;
	private double distance;
	
	//JTS Orientation
	public static final int COUNTER_CLOCKWISE = 1; //Left
	public static final int STRAIGHT = 0; 
	public static final int CLOCKWISE = -1; //Right
	
	private static final Color BRAKE_COLOR = Color.RED;
	private static final Color FREE_COLOR = Color.GREEN;
	private static final Color FROZEN_COLOR = Color.BLUE;
	
	//Reference : https://copradar.com/chapts/references/acceleration.html
	private static final double SPEED = 40.0; //mph
	private static final double ACC = 18.6; //18.6mph/s
	private static final double DEC = 18.6; //18.6mph/s
	
	private static final double POLITE = 0.3;
	private static final double MAX_SAFE_DEC = 18.6;
	private static final double THRESHOLD_ACC = 18.6;
	
	LinearInterpolator interp = new LinearInterpolator();
	PolynomialSplineFunction f;
	GeometryFactory gf=new GeometryFactory();
	private Angle angle;
	private static PrecisionModel precision = new PrecisionModel();
	private static int SRID = new Random().nextInt()*Integer.MAX_VALUE;

	private Coordinate[] route;
	private double[] x;
	private double[] y;
	private Coordinate curCoordinate;
//	private Polygon buffer;

	private double speed = 0;
	private double dt = 0.2; //0.2 seconds per move
	
	//create a looking ahead rectangle and looking back rectangle by current position
	private Coordinate[] closestAhead;
	private Coordinate[] closetsBack;
	
	private Polygon aheadRect;
	private Polygon backRect;
	private double aheadWidth;
	private double backWidth;
	private double lookLength;
	
	public Vehicle(Coordinate[] points) {
		super(points, precision, SRID);
		setCurCoordinate(points[0]);
		this.route = points;
		
		//initial interpolator
		int len = points.length;
		this.x = new double[len];
		this.y = new double[len];
		for(int i = 0; i<len; i++) {
			this.x[i] = points[i].x;
			this.y[i] = points[i].y;
		}
		f = interp.interpolate(this.x, this.y);
		
		//initial angle list
		
		
	}
	
	public void apply(Coordinate source, Coordinate destination, double distance, long time) {
		this.source = source;
		this.destination = destination;
		this.distance = distance;
		this.time = time;
	}
	
	public Coordinate getCurCoordinate() {
		return curCoordinate;
	}

	//need check buffer distance
	public void setCurCoordinate(Coordinate coor) {
		this.curCoordinate = coor;
		//buffer = (Polygon) gf.createPoint(curCoordinate).buffer(1.0);
	}
	

	public Coordinate getCoordinateN(int index) {
		int total = route.length;
		
		if(index >= total) {
			int newIdx = index % total;
			return route[newIdx];
		}else {
			return route[index];
		}
	}
	
	//0.2
//	public Coordinate getNext() {
//		
//	}
	
	private void move(Coordinate a, Coordinate b, double dt) {
		double heading = angle.angle(a, b);
		double dx = Math.cos(heading) * speed * dt;
		double dy = Math.sin(heading) * speed * dt;
		//current + dx
		//current + dy
	}

	private void moderateSpeed(double dt) {
			
	}
	
	public Color getColor(boolean showTrueColor) {
		return FREE_COLOR;
	}
	
}
