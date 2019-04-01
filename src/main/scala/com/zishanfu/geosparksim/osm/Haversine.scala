package com.zishanfu.geosparksim.osm

object Haversine {
  val RADIUS_OF_EARTH = 6371;

  /**
    *
    * @param lat1
    * @param long1
    * @param lat2
    * @param long2
    * @return
    */
  def haversine(lat1: Double, long1: Double, lat2: Double, long2: Double): Double = {
    val latitude1 = Math.toRadians(lat1);
		val latitude2 = Math.toRadians(lat2);
		val longitude1 = Math.toRadians(long1);
		val longitude2 = Math.toRadians(long2);

		1000 * 2 * RADIUS_OF_EARTH * Math.asin(Math.sqrt(
				Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
				Math.cos(latitude1) * Math.cos(latitude2) *
				Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2)));
  }

}
