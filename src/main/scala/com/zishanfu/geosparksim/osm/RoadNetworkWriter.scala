package com.zishanfu.geosparksim.osm


import com.zishanfu.geosparksim.Model.{Intersect, Link, TrafficLight}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row, SparkSession}


class RoadNetworkWriter(sparkSession: SparkSession, roadNetwork: RoadNetwork, path: String){

  def writeEdgeJson(): Unit = {
    val edges : RDD[Link] = roadNetwork.links
    val map = edges.map((edge: Link) => Row(
      edge.getId,
      Row(edge.getHead.id, Row(edge.getHead.coordinate.x, edge.getHead.coordinate.y), edge.getHead.signal, edge.getHead.intersect),
      Row(edge.getTail.id, Row(edge.getTail.coordinate.x, edge.getTail.coordinate.y), edge.getTail.signal, edge.getTail.intersect),
      edge.getDistance,
      edge.getSpeed,
      edge.getDriveDirection,
      edge.getLanes,
      edge.getAngle,
      edge.getLaneArray
    ))
    val df = sparkSession.createDataFrame(map, Define.linkSchema)
    df.write.format("json").save(path + "/edges.json")
  }


  def writeSignalJson(): Unit = {
    val signals : RDD[TrafficLight] = roadNetwork.lights
    val map = signals.map((signal: TrafficLight) => Row(
      Row(signal.getCoordinate.x, signal.getCoordinate.y),
      signal.getSRID,
      signal.getWid,
      Row(signal.getLocation.x, signal.getLocation.y)
    ))
    val df = sparkSession.createDataFrame(map, Define.signalSchema)
    df.write.format("json").save(path + "/signals.json")
  }


  def writeIntersectJson(): Unit = {
    val intersects : RDD[Intersect] = roadNetwork.intersects
    val map = intersects.map((intersect: Intersect) => Row(
      Row(intersect.getCoordinate.x, intersect.getCoordinate.y),
      intersect.getSRID,
      intersect.getWid
    ))
    val df = sparkSession.createDataFrame(map, Define.intersectSchema)
    df.write.format("json").save(path + "/intersects.json")
  }


}