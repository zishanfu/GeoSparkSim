package com.zishanfu.geosparksim.osm

import com.vividsolutions.jts.geom.Point
import com.zishanfu.geosparksim.model._
import org.apache.spark.sql.{Encoder, Encoders}
import org.apache.spark.sql.types._

object Define {
  val lane_width: Double = 0.00003

  val PointEncoder: Encoder[Point] = Encoders.kryo[Point]
  val linkEncoder: Encoder[Link] = Encoders.kryo[Link]
  val signalEncoder: Encoder[TrafficLight] = Encoders.kryo[TrafficLight]
  val intersectEncoder: Encoder[Intersect] = Encoders.kryo[Intersect]
  val vehicleEncoder: Encoder[Vehicle] = Encoders.kryo[Vehicle]
  val mobilVehEncoder: Encoder[MOBILVehicle] = Encoders.kryo[MOBILVehicle]
  val reportEncoder: Encoder[StepReport] = Encoders.kryo[StepReport]

  val coordinateSchema = StructType(Array(
    StructField("Latitude", DoubleType, nullable = false),
    StructField("Longitude", DoubleType, nullable = false)
  ))

  val nodeSchema = StructType(Array(
    StructField("Id", LongType, nullable = false),
    StructField("Coordinate", coordinateSchema, nullable = false),
    StructField("Signal", BooleanType, nullable = false),
    StructField("Intersect", BooleanType, nullable = false)
  ))

  val signalSchema = StructType(Array(
    StructField("Coordinate", coordinateSchema, nullable = false),
    StructField("Nid", IntegerType, nullable = false),
    StructField("Wid", LongType, nullable = false),
    StructField("Location", coordinateSchema, nullable = false)
  ))

  val intersectSchema = StructType(Array(
    StructField("Coordinate", coordinateSchema, nullable = false),
    StructField("Nid", IntegerType, nullable = false),
    StructField("Wid", LongType, nullable = false)
  ))

  val linkSchema = StructType(Array(
    StructField("Id", LongType, nullable = false),
    StructField("Head", nodeSchema, nullable = false),
    StructField("Tail", nodeSchema, nullable = false),
    StructField("Distance", DoubleType, nullable = false),
    StructField("Speed", IntegerType, nullable = false),
    StructField("DriveDirection", IntegerType, nullable = false),
    StructField("Lanes", IntegerType, nullable = false),
    StructField("Angle", DoubleType, nullable = false),
    StructField("LaneArray", StringType, nullable = false)
  ))

  val vehicleSchema = StructType(Array(
    StructField("Id", StringType, nullable = false),
    StructField("Source", coordinateSchema, nullable = false),
    StructField("Target", coordinateSchema, nullable = false),
    StructField("EdgePath", ArrayType(LongType, containsNull = false), nullable = false),
    StructField("Costs", ArrayType(DoubleType, containsNull = false), nullable = false),
    StructField("CoordinatePath", ArrayType(StringType, containsNull = false), nullable = false)
  ))

  val reportSchema = StructType(Array(
    StructField("Step", IntegerType, nullable = false),
    StructField("Vehicle Id", StringType, nullable = true),
    StructField("Vehicle Front", coordinateSchema, nullable = true),
    StructField("Vehicle Rear", coordinateSchema, nullable = true),
    StructField("Signal", IntegerType, nullable = true),
    StructField("Signal Location", coordinateSchema, nullable = true)
  ))

}
