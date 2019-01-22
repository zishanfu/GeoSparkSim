package com.zishanfu.vistrips.tools;


import org.geotools.geometry.jts.JTSFactoryFinder;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class Utils {
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	private Angle angle;
	
	/**
	 * @param PointList pl
	 * @return LineString
	 */
	public LineString toLineString(PointList pl) {
		return geometryFactory.createLineString(toCoordinates(pl));
	}
	
	/**
	 * @param PointList pl
	 * @return Coordinate[]
	 */
	public Coordinate[] toCoordinates(PointList pl) {
		int len = pl.getSize();
		Coordinate[] coordinates = new Coordinate[len];
		for(int i = 0; i<len; i++) {
			coordinates[i] = new Coordinate(pl.getLon(i), pl.getLat(i));
		}
		return coordinates;
	}
	
	public double heading(Coordinate p0, Coordinate p1) {
		return angle.angle(p0, p1);
	}
	
	public double angleDiff(double ang1, double ang2) {
		return angle.diff(ang1, ang2);
	}
	
	public int getTurn(double ang1, double ang2) {
		return angle.getTurn(ang1, ang2);
	}
}
