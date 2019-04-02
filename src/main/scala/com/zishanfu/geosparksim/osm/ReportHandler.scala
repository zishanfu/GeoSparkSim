package com.zishanfu.geosparksim.osm

import com.vividsolutions.jts.geom.Coordinate
import com.zishanfu.geosparksim.Model.StepReport
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row, SparkSession}

class ReportHandler (sparkSession: SparkSession, path: String, numPartition: Int){

  def readReportJson(): Dataset[StepReport] ={
    val reports = sparkSession.sqlContext.read.schema(Define.reportSchema).json(path + "/reports*")
    val reportDS : Dataset[StepReport] = reports.map(report => {
      val step = report.getAs[Int]("Step")
      val vehicleId = report.getAs[String]("Vehicle Id")
      if(vehicleId != null){
        val frontStruct = report.getStruct(report.fieldIndex("Vehicle Front"))
        val front_coordinate_latitude = frontStruct.getAs[Double]("Latitude")
        val front_coordinate_longitude = frontStruct.getAs[Double]("Longitude")

        val rearStruct = report.getStruct(report.fieldIndex("Vehicle Rear"))
        val rear_coordinate_latitude = rearStruct.getAs[Double]("Latitude")
        val rear_coordinate_longitude = rearStruct.getAs[Double]("Longitude")

        new StepReport(step, null, 0,
          vehicleId, new Coordinate(front_coordinate_latitude, front_coordinate_longitude), new Coordinate(rear_coordinate_latitude, rear_coordinate_longitude))
      }else{
        val signal = report.getAs[Int]("Signal")
        val signalLocation = report.getStruct(report.fieldIndex("Signal Location"))
        val signal_coordinate_latitude = signalLocation.getAs[Double]("Latitude")
        val signal_coordinate_longitude = signalLocation.getAs[Double]("Longitude")

        new StepReport(step, new Coordinate(signal_coordinate_latitude, signal_coordinate_longitude), signal,
          vehicleId, null, null)
      }

      //(step: Int, signalLocation: Coordinate, signal: Int, vehicleId: String, vehicleFront: Coordinate, vehicleRear: Coordinate)

    })(Define.reportEncoder)

    reportDS
  }

  def writeReportJson(reports: RDD[StepReport], id: Int): Unit ={
    val map = reports.filter(r => r.getVehicleFront != null || r.getSignalLocation != null).map((report: StepReport) => {
      if (report.getVehicleFront != null){
        Row(
          report.getStep,
          report.getVehicleId,
          Row(report.getVehicleFront.x, report.getVehicleFront.y),
          Row(report.getVehicleRear.x, report.getVehicleRear.y),
          null,
          null
        )
      }else{
        Row(
          report.getStep,
          null,
          null,
          null,
          report.getSignal,
          Row(report.getSignalLocation.x, report.getSignalLocation.y)
        )
      }
    })

    val df = sparkSession.createDataFrame(map, Define.reportSchema)
    df.write.format("json").save(path + "/reports" + id)
  }

}
