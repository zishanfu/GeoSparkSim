package com.zishanfu.geosparksim.osm


import com.vividsolutions.jts.geom.Coordinate
import com.zishanfu.geosparksim.model.{Intersect, Link, TrafficLight}
import com.zishanfu.geosparksim.model.SegmentNode
import org.apache.spark.sql.{Dataset, SparkSession}

class RoadNetworkReader (sparkSession: SparkSession, path: String){

  def readEdgeJson(): Dataset[Link] = {
    val edges = sparkSession.sqlContext.read.schema(Define.linkSchema).json(path + "/edges.json")
    val linkDS : Dataset[Link] = edges.map(edge => {
      val id = edge.getAs[Long]("Id")

      val head_struct = edge.getStruct(edge.fieldIndex("Head"))
      val head_id = head_struct.getAs[Long]("Id")
      val head_coordinate_struct = head_struct.getStruct(head_struct.fieldIndex("Coordinate"))

      val head_coordinate_latitude = head_coordinate_struct.getAs[Double]("Latitude")
      val head_coordinate_longitude = head_coordinate_struct.getAs[Double]("Longitude")

      val head_signal = head_struct.getAs[Boolean]("Signal")
      val head_intersect = head_struct.getAs[Boolean]("Intersect")

      val tail_struct = edge.getStruct(edge.fieldIndex("Tail"))
      val tail_id = tail_struct.getAs[Long]("Id")
      val tail_coordinate_struct = tail_struct.getStruct(tail_struct.fieldIndex("Coordinate"))

      val tail_coordinate_latitude = tail_coordinate_struct.getAs[Double]("Latitude")
      val tail_coordinate_longitude = tail_coordinate_struct.getAs[Double]("Longitude")

      val tail_signal = tail_struct.getAs[Boolean]("Signal")
      val tail_intersect = tail_struct.getAs[Boolean]("Intersect")

      val distance = edge.getAs[Double]("Distance")
      val speed = edge.getAs[Int]("Speed")
      val direction = edge.getAs[Int]("DriveDirection")
      val lanes = edge.getAs[Int]("Lanes")
      val angle = edge.getAs[Double]("Angle")
      val laneArray = edge.getAs[String]("LaneArray")

      val head = SegmentNode(head_id, new Coordinate(head_coordinate_latitude, head_coordinate_longitude), head_signal, head_intersect)
      val tail = SegmentNode(tail_id, new Coordinate(tail_coordinate_latitude, tail_coordinate_longitude), tail_signal, tail_intersect)

      val path: Array[Coordinate] = Array(head.coordinate, tail.coordinate)
      new Link(id, head, tail, distance, speed, direction, lanes, angle, laneArray, path)
    })(Define.linkEncoder)

    linkDS
  }


  def readSignalJson(): Dataset[TrafficLight] = {
    val signals = sparkSession.sqlContext.read.schema(Define.signalSchema).json(path + "/signals.json")
    val trafficLightDS = signals.map(signal => {
      val coordinate_struct = signal.getStruct(signal.fieldIndex("Coordinate"))
      val coordinate_latitude = coordinate_struct.getAs[Double]("Latitude")
      val coordinate_longitude = coordinate_struct.getAs[Double]("Longitude")

      val location_struct = signal.getStruct(signal.fieldIndex("Location"))
      val location_latitude = location_struct.getAs[Double]("Latitude")
      val location_longitude = location_struct.getAs[Double]("Longitude")

      val nid = signal.getAs[Int]("Nid")
      val wid = signal.getAs[Long]("Wid")
      new TrafficLight(new Coordinate(coordinate_latitude, coordinate_longitude), nid, wid, new Coordinate(location_latitude, location_longitude))
    })(Define.signalEncoder)

    trafficLightDS
  }

  def readIntersectJson(): Dataset[Intersect] = {
    val intersects = sparkSession.sqlContext.read.schema(Define.intersectSchema).json(path + "/intersects.json")
    val intersectDS = intersects.map(intersect => {
      val coordinate_struct = intersect.getStruct(intersect.fieldIndex("Coordinate"))
      val coordinate_latitude = coordinate_struct.getAs[Double]("Latitude")
      val coordinate_longitude = coordinate_struct.getAs[Double]("Longitude")
      val nid = intersect.getAs[Int]("Nid")
      val wid = intersect.getAs[Long]("Wid")
      new Intersect(new Coordinate(coordinate_latitude, coordinate_longitude), nid, wid)
    })(Define.intersectEncoder)

    intersectDS
  }
}
