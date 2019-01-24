package com.zishanfu.vistrips.sim

import com.zishanfu.vistrips.osm.OsmGraph
import com.zishanfu.vistrips.sim.model.IDMVehicle
import org.apache.spark.rdd.RDD
import com.vividsolutions.jts.geom.Coordinate


case class World (graph: OsmGraph, vehicle: RDD[IDMVehicle]){
  val roadVehicles = roadDigesting
  
  def getRoadVehicles: RDD[IDMVehicle] = roadVehicles
  
  private def roadDigesting() : RDD[IDMVehicle] = {
    val signals = graph.getIntersectsSet
    val intersects = graph.getSignalsSet
    val edges = graph.graph.edges
    
    val landedVehicles = vehicle.map(vehicle => {
      val coordinates = vehicle.getCoordinates
      val gps = vehicle.getGps
      coordinates.foreach(coor => {
        if(signals.contains(coor)){
          gps.addLights(coor)
        }else if(intersects.contains(coor)){
          gps.addIntersects(coor)
        }
      })
      vehicle.setGps(gps)
      vehicle
    })
    
//    val rddEdgeBykey = edges.keyBy(link => {
//      val head = link.attr.head
//      val tail = link.attr.tail
//      val p = new Coordinate(head.getX, head.getY)
//      val q = new Coordinate(tail.getX, tail.getY)
//      (p, q)
//    })
//    val rddVehicleBykey = vehicle.keyBy(vehicle => {
//      
//    })
    landedVehicles
  }
}