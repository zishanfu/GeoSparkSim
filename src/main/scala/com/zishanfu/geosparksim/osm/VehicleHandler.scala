package com.zishanfu.geosparksim.osm

import com.vividsolutions.jts.geom.Coordinate
import com.zishanfu.geosparksim.model.{MOBILVehicle, Vehicle}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.collection.mutable

class VehicleHandler (sparkSession: SparkSession, path: String){
  def readVehicleTrajectoryJson(): Dataset[MOBILVehicle] = {
    val vehicles = sparkSession.sqlContext.read.schema(Define.vehicleSchema).json(path + "/vehicles.json")
    val vehicleDS = vehicles.map(vehicle => {
      val id = vehicle.getAs[String]("Id")
      val source_struct = vehicle.getStruct(vehicle.fieldIndex("Source"))
      val source_latitude = source_struct.getAs[Double]("Latitude")
      val source_longitude = source_struct.getAs[Double]("Longitude")

      val target_struct = vehicle.getStruct(vehicle.fieldIndex("Target"))
      val target_latitude = target_struct.getAs[Double]("Latitude")
      val target_longitude = target_struct.getAs[Double]("Longitude")

      val edges : Array[java.lang.Long] = vehicle.getAs[mutable.WrappedArray[java.lang.Long]]("EdgePath").array
      val costs : Array[java.lang.Double] = vehicle.getAs[mutable.WrappedArray[java.lang.Double]]("Costs").array
      val coordinates : Array[java.lang.String] = vehicle.getAs[mutable.WrappedArray[java.lang.String]]("CoordinatePath").array

      val fullPath : java.util.ArrayList[Coordinate] = new java.util.ArrayList[Coordinate]

      for (coordinate <- coordinates){
        val str : Array[String] = coordinate.substring(1, coordinate.length).split(",")
        fullPath.add(new Coordinate(str(0).toDouble, str(1).toDouble))
      }

      val expectedPath = Array.ofDim[Coordinate](fullPath.size())

      for (i <- 0 until fullPath.size()){
        expectedPath(i) = fullPath.get(i)
      }

      new MOBILVehicle(id, new Coordinate(source_latitude, source_longitude), new Coordinate(target_latitude, target_longitude), edges, costs, fullPath)
    })(Define.mobilVehEncoder)

    vehicleDS
  }

  //(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath)
  def writeVehicleTrajectoryJson(vehicles: Seq[Vehicle]): Unit = {
    val vehiclesRDD : RDD[Vehicle] =sparkSession.sparkContext.parallelize(vehicles, 10)
    val map = vehiclesRDD.map((vehicle: Vehicle) => Row(
      vehicle.getId,
      Row(vehicle.getSource.x, vehicle.getSource.y),
      Row(vehicle.getTarget.x, vehicle.getTarget.y),
      vehicle.getEdgePath,
      vehicle.getCosts,
      vehicle.getCoordinateString
    ))
    val df = sparkSession.createDataFrame(map, Define.vehicleSchema)
    df.write.format("json").save(path + "/vehicles.json")
  }
}
