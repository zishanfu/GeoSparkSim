package com.zishanfu.vistrips.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class Interpolate implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5906318848796881434L;
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	
	//time in seconds, distance in meters
//	public LineString routeInterpolate(LineString origin, long time, double distance) {
//		double avgSpeed = distance / time; // m/s
//		int num = origin.getNumPoints();
//			
//		if(num < 2) return origin;
//		List<Coordinate> coordinates = new ArrayList<>();
//		coordinates.add(origin.getCoordinateN(0));
//		for(int i = 0; i< num - 1; i++) {
//			Coordinate src = origin.getCoordinateN(i);
//			Coordinate dst = origin.getCoordinateN(i+1);
//			double distStep = new Distance().haversine(src.y, src.x, dst.y, dst.x);
//			double stepInSec = distStep / avgSpeed; 
//			if(distStep > avgSpeed) {
//				int steps = (int) stepInSec;
//				for(int j = 0; j < steps; j++) {
//					coordinates.add(linearInterpolate(src, dst, distStep, j));
//				}
//			}
//			coordinates.add(dst);
//		}
//			
//		Coordinate[] coorArr = new Coordinate[coordinates.size()];
//		return geometryFactory.createLineString(coordinates.toArray(coorArr));
//	}
	
	public LineString routeInterpolateBySec(LineString origin, long time, double distance, double sec) {
		double avgSpeed = distance / time; // m/s
		double stepLength = avgSpeed * sec;
		int num = origin.getNumPoints();
		if(num < 2) return origin;
		
		List<Coordinate> coordinates = new ArrayList<>();
		
		
		for(int i = 0; i< num - 1; i++) {
			Coordinate src = origin.getCoordinateN(i);
			Coordinate dst = origin.getCoordinateN(i+1);
			double distStep = new Distance().haversine(src.y, src.x, dst.y, dst.x);
			double stepInStep = distStep / stepLength; 
			
			coordinates.add(origin.getCoordinateN(i));
			
			if(stepInStep > 1.0) {
				int steps = (int) stepInStep;
				for(int j = 0; j < steps; j++) {
					coordinates.add(linearInterpolate(src, dst, distStep, j));
				}
			}
			coordinates.add(dst);
		}
			
		Coordinate[] coorArr = new Coordinate[coordinates.size()];
		return geometryFactory.createLineString(coordinates.toArray(coorArr));
	}
		
	private Coordinate linearInterpolate(Coordinate src, Coordinate dst, double d, double n) {
		double x = src.x + n/d * (dst.x - src.x);
		double y = src.y + n/d * (dst.y - src.y);
		return new Coordinate(x, y);
	}	
}
