package com.zishanfu.vistrips.tools;

import java.awt.Point;
import java.lang.Math;

import com.vividsolutions.jts.geom.Coordinate;

public class SphericalMercator {
  private final double RADIUS = 6378137.0; /* in meters on the equator */

  /* These functions take their length parameter in meters and return an angle in degrees */

  private double y2lat(double aY) {
    return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - Math.PI/2);
  }
  
  private double x2lon(double aX) {
    return Math.toDegrees(aX / RADIUS);
  }

  /* These functions take their angle parameter in degrees and return a length in meters */

  private double lat2y(double aLat) {
    return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(aLat) / 2)) * RADIUS;
  }  
  
  private double lon2x(double aLong) {
    return Math.toRadians(aLong) * RADIUS;
  }
  
  public Coordinate point2Coordinate(Point point, int zoom) {
	  zoom = 1 << zoom;
	  point.x = point.x * zoom;
      point.y = point.y * zoom; 
	  return new Coordinate(x2lon(point.x), y2lat(point.y));
  }
  
  public Point coordinate2Point(Coordinate coor, int zoom) {
	  Point point = new Point((int)lon2x(coor.x), (int)lat2y(coor.y));
	  zoom = 1 << zoom;
      point.x = point.x / zoom;
      point.y = point.y / zoom;
	  return point;
  }
  
}