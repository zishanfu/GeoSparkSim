package com.zishanfu.vistrips.path

import com.vividsolutions.jts.geom.Point

object Haversine {
  val RADIUS_OF_EARTH = 6371;
  
  private def haversine(lat1: Double, long1: Double, lat2: Double, long2: Double): Double = {
    val latitude1 = Math.toRadians(lat1);
		val latitude2 = Math.toRadians(lat2);
		val longitude1 = Math.toRadians(long1);
		val longitude2 = Math.toRadians(long2);
		
		(1000 * 2 * RADIUS_OF_EARTH * Math.asin(Math.sqrt(
				Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
				Math.cos(latitude1) * Math.cos(latitude2) *
				Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2))));
  }
  
  def harversineMile(p1: Point, p2: Point): Double = {
    haversine(p1.getY(), p1.getX(), p2.getY(), p2.getX())*0.00062137;
  }
}