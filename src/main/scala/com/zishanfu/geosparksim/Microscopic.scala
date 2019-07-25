package com.zishanfu.geosparksim

import java.util
import java.util.Random

import com.vividsolutions.jts.geom.Coordinate
import com.zishanfu.geosparksim.Model._
import com.zishanfu.geosparksim.osm.{Define, ReportHandler}
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}
import org.datasyslab.geospark.enums.GridType
import org.datasyslab.geospark.spatialRDD.SpatialRDD

import scala.collection.JavaConverters._
import scala.util.control.Breaks._

object Microscopic {
  @transient lazy val logger = Logger.getLogger(getClass.getName)

  /**
    * Initialize edges to simulation links, for example, dividing lanes, defining lane center and boundary.
    *
    * @param edges the road network edges
    * @return simulation links
    */
  def init(edges: Dataset[Link]): Dataset[Link] = {
    //initial edge
    val newEdges = edges.map(edge => {
      val laneStrings = edge.getLaneArray.split("#")
      val laneMatrix = Array.ofDim[Coordinate](laneStrings.length - 1, 2)
      val laneCenterMatrix = Array.ofDim[Coordinate](laneStrings.length - 1, 2)

      for(i <- 1 until laneStrings.length){
        val lane = laneStrings(i)
        val lane_items = lane.split("\\|")
        val headLine = lane_items(1).split(",")
        val tailLine = lane_items(2).split(",")

        val laneHead = new Coordinate(headLine(0).toDouble, headLine(1).toDouble)
        val laneTail = new Coordinate(tailLine(0).toDouble, tailLine(1).toDouble)

        val headCenter = lane_items(3).split(",")
        val tailCenter = lane_items(4).split(",")
        val laneCenterHead = new Coordinate(headCenter(0).toDouble, headCenter(1).toDouble)
        val laneCenterTail = new Coordinate(tailCenter(0).toDouble, tailCenter(1).toDouble)

        laneMatrix(i - 1) = Array(laneHead, laneTail)
        laneCenterMatrix(i - 1) = Array(laneCenterHead, laneCenterTail)
      }

      var minLat = Math.min(edge.getHead.coordinate.x, edge.getTail.coordinate.x)
      var maxLat = Math.max(edge.getHead.coordinate.x, edge.getTail.coordinate.x)
      var minLon = Math.min(edge.getHead.coordinate.y, edge.getTail.coordinate.y)
      var maxLon = Math.max(edge.getHead.coordinate.y, edge.getTail.coordinate.y)

      for (i <- laneMatrix.indices){
        for (j <- 0 to 1){
          minLat = Math.min(laneMatrix(i)(j).x, minLat)
          maxLat = Math.max(laneMatrix(i)(j).x, maxLat)
          minLon = Math.min(laneMatrix(i)(j).y, minLon)
          maxLon = Math.max(laneMatrix(i)(j).y, maxLon)
        }
      }

      edge.setLaneMatrix(laneMatrix)
      edge.setLaneCenterMatrix(laneCenterMatrix)
      edge.setBoundary(minLat, maxLat, minLon, maxLon)
      edge.initLaneVehicles()

      edge
    })(Define.linkEncoder)

    newEdges
  }

  /**
    *
    * @param sparkSession the spark session
    * @param edges edges
    * @param signals traffic signals
    * @param intersects intersects
    * @param vehicles simulation vehicles
    * @param path the result path
    * @param steps simulation steps
    * @param timestep time per step
    * @param numPartition the number of partitions
    */
  def sim(sparkSession: SparkSession, edges: Dataset[Link], signals: Dataset[TrafficLight], intersects: Dataset[Intersect], vehicles: Dataset[MOBILVehicle],
          path: String, steps: Int, timestep: Double, numPartition: Int): Unit = {
    val rand = new Random
    val vehicleRDD = new SpatialRDD[MOBILVehicle]
    vehicleRDD.setRawSpatialRDD(vehicles.rdd)
    vehicleRDD.analyze

    val edgeRDD = new SpatialRDD[Link]
    edgeRDD.setRawSpatialRDD(init(edges).rdd)
    edgeRDD.rawSpatialRDD = edgeRDD.rawSpatialRDD.cache()
    edgeRDD.analyze

    val signalRDD = new SpatialRDD[TrafficLight]
    signalRDD.setRawSpatialRDD(signals.rdd)
    signalRDD.analyze

    val intersectRDD = new SpatialRDD[Intersect]
    intersectRDD.setRawSpatialRDD(intersects.rdd)
    intersectRDD.analyze

    vehicleRDD.spatialPartitioning(GridType.KDBTREE, numPartition)
    edgeRDD.spatialPartitioning(vehicleRDD.getPartitioner)
    signalRDD.spatialPartitioning(vehicleRDD.getPartitioner)
    intersectRDD.spatialPartitioning(vehicleRDD.getPartitioner)

    //landing simulation objects
    val reportRDD0 = vehicleRDD.spatialPartitionedRDD.rdd.zipPartitions(edgeRDD.spatialPartitionedRDD.rdd, signalRDD.spatialPartitionedRDD.rdd, true){
      (vehicleIt, edgeIt, signalIt) => {
        val edgeList = edgeIt.toList
        val signalList = signalIt.toList
        var stepReports = scala.List.empty[StepReport]

        var edgeMap = scala.collection.mutable.Map.empty[java.lang.Long, java.util.List[Link]].asJava

        var edge_minLat = Double.PositiveInfinity
        var edge_maxLat = Double.NegativeInfinity
        var edge_minLon = Double.PositiveInfinity
        var edge_maxLon = Double.NegativeInfinity

        for(edge <- edgeList){
          if(!edgeMap.containsKey(edge.getId)) edgeMap.put(edge.getId, new util.ArrayList[Link])
          edge_minLat = Math.min(edge.getMinLat, edge_minLat)
          edge_maxLat = Math.max(edge.getMaxLat, edge_maxLat)
          edge_minLon = Math.min(edge.getMinLon, edge_minLon)
          edge_maxLon = Math.max(edge.getMaxLon, edge_maxLon)
          edgeMap.get(edge.getId).add(edge)
        }

        //initialize traffic signals
        val signalNodeMap = new java.util.HashMap[Int, java.util.List[TrafficLight]]
        for (light <- signalList){
          val nid = light.getSRID
          if(!signalNodeMap.containsKey(nid)) signalNodeMap.put(nid, new util.ArrayList[TrafficLight])
          signalNodeMap.get(nid).add(light)
        }

        val signalWayMap = scala.collection.mutable.Map.empty[java.lang.Long, TrafficLight].asJava

        for ( nid <- signalNodeMap.keySet().asScala){
          val four = signalNodeMap.get(nid)
          breakable{
            if(four.size < 4) break()
            val w1 = four.get(0)
            val w1_angle = Math.abs(edgeMap.get(w1.getWid).get(0).getAngle)
            val w2 = four.get(1)
            val w2_angle = Math.abs(edgeMap.get(w2.getWid).get(0).getAngle)
            val w3 = four.get(2)
            val w3_angle = Math.abs(edgeMap.get(w3.getWid).get(0).getAngle)
            val w4 = four.get(3)
            val select = rand.nextInt(2)
            w1.setTime(0)
            w2.setTime(0)
            w3.setTime(0)
            w4.setTime(0)
            val select_signal = if (select == 1) 2 else 0
            val other_signal = if ((select + 1) % 2 == 1) 2 else 0
            import Model.StepReport
            if (Math.abs(w3_angle - w1_angle) < 0.1) {
              w1.setSignal(select_signal)
              w3.setSignal(select_signal)
              w2.setSignal(other_signal)
              w4.setSignal(other_signal)
            }
            else if (Math.abs(w3_angle - w2_angle) < 0.1) {
              w3.setSignal(select_signal)
              w2.setSignal(select_signal)
              w1.setSignal(other_signal)
              w4.setSignal(other_signal)
            }
            else {
              w3.setSignal(select_signal)
              w4.setSignal(select_signal)
              w1.setSignal(other_signal)
              w2.setSignal(other_signal)
            }

            signalWayMap.put(w1.getWid, w1)
            signalWayMap.put(w2.getWid, w2)
            signalWayMap.put(w3.getWid, w3)
            signalWayMap.put(w4.getWid, w4)

            stepReports = stepReports:+ new StepReport(0, w1.getLocation, w1.getWid, w1.getSignal, w1.getTime)
            stepReports = stepReports:+ new StepReport(0, w2.getLocation, w2.getWid, w2.getSignal, w2.getTime)
            stepReports = stepReports:+ new StepReport(0, w3.getLocation, w3.getWid, w3.getSignal, w3.getTime)
            stepReports = stepReports:+ new StepReport(0, w4.getLocation, w4.getWid, w4.getSignal, w4.getTime)
          }
        }

        val vehicleList = vehicleIt.toList

        vehicleList.foreach(vehicle => {
          vehicle.initVehicle()
          edgeMap = vehicle.born(edgeMap, "Baby")
          stepReports = stepReports:+ new StepReport(0, vehicle.getId, vehicle.getFront, vehicle.getRear, vehicle.getSource, vehicle.getTarget, vehicle.getEdgePath, vehicle.getCosts, vehicle.getFullPath,
            vehicle.getEdgeIndex, vehicle.getCurrentLane, vehicle.getPosition, vehicle.getVelocity, vehicle.getCurrentLink, vehicle.getHeadSignal)
        })

        stepReports.iterator
      }
    }

    val reportHandler = new ReportHandler(sparkSession, path, numPartition)
    reportHandler.writeReportJson(reportRDD0, 0)

    var execTime = 0.0

    val t1 = System.currentTimeMillis()
    val recoverTuple = recovery(vehicleRDD.getRawSpatialRDD.rdd, signalRDD.getRawSpatialRDD.rdd, reportRDD0, 0, numPartition)

    vehicleRDD.setRawSpatialRDD(recoverTuple._1)
    signalRDD.setRawSpatialRDD(recoverTuple._2)
    vehicleRDD.spatialPartitioning(GridType.KDBTREE, numPartition)
    edgeRDD.spatialPartitioning(vehicleRDD.getPartitioner)
    signalRDD.spatialPartitioning(vehicleRDD.getPartitioner)
    val t2 = System.currentTimeMillis()
    execTime = execTime + t2 - t1

    //if the number of vehicles is larger than 100k,
    // GeoSparkSim will do sample simulation to find the best repartition period with minimum time cost
    val bestRepartition = if(vehicleRDD.getRawSpatialRDD.count() < 100000) steps/5 else repartitionCriterion(vehicleRDD, signalRDD, edgeRDD, steps, timestep, numPartition)

    val newSteps = (steps / timestep).toInt

    val iteration = newSteps / bestRepartition + (if (newSteps % bestRepartition == 0) 0 else 1)

    logger.warn("best repartition: " + bestRepartition)

    for(n <- 1 to iteration){

      val reportRDD : RDD[StepReport] = vehicleRDD.spatialPartitionedRDD.rdd.zipPartitions(edgeRDD.spatialPartitionedRDD.rdd, signalRDD.spatialPartitionedRDD.rdd, true){
        (vehicleIt, edgeIt, signalIt) => {
          val edgeList = edgeIt.toList
          val signalList = signalIt.toList
          var stepReports = scala.List.empty[StepReport]

          var edgeMap = scala.collection.mutable.Map.empty[java.lang.Long, java.util.List[Link]].asJava
          var edge_minLat = Double.PositiveInfinity
          var edge_maxLat = Double.NegativeInfinity
          var edge_minLon = Double.PositiveInfinity
          var edge_maxLon = Double.NegativeInfinity

          for(edge <- edgeList){
            if(!edgeMap.containsKey(edge.getId)) edgeMap.put(edge.getId, new util.ArrayList[Link])
            edge_minLat = Math.min(edge.getMinLat, edge_minLat)
            edge_maxLat = Math.max(edge.getMaxLat, edge_maxLat)
            edge_minLon = Math.min(edge.getMinLon, edge_minLon)
            edge_maxLon = Math.max(edge.getMaxLon, edge_maxLon)
            edgeMap.get(edge.getId).add(edge)
          }

          //initialize traffic signals
          val signalWayMap = scala.collection.mutable.Map.empty[java.lang.Long, TrafficLight].asJava
          for(signal <- signalList){
            signalWayMap.put(signal.getWid, signal)
          }
          //initialize vehicle position
          val vehicleList = vehicleIt.toList

          vehicleList.foreach(vehicle => {
            edgeMap = vehicle.born(edgeMap, "Sync")
          })

          for(i <- bestRepartition*(n-1)+1 to bestRepartition*n){
            if(i <= steps){
              for (wid <- signalWayMap.keySet().asScala){
                val light = signalWayMap.get(wid)
                light.next(1)
                signalWayMap.put(wid, light)
                stepReports = stepReports:+ new StepReport(i, light.getLocation, light.getWid, light.getSignal, light.getTime)
              }

              // Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath
              vehicleList.foreach(vehicle => {
                if(vehicle.getFront.x < edge_minLat || vehicle.getFront.x > edge_maxLat || vehicle.getFront.y < edge_minLon || vehicle.getFront.y > edge_maxLon){
                  stepReports = stepReports:+ new StepReport(i, vehicle.getId, vehicle.getFront, vehicle.getRear, vehicle.getSource, vehicle.getTarget, vehicle.getEdgePath, vehicle.getCosts, vehicle.getFullPath,
                    vehicle.getEdgeIndex, vehicle.getCurrentLane, vehicle.getPosition, vehicle.getVelocity, vehicle.getCurrentLink, vehicle.getHeadSignal)
                }else{
                  if(!vehicle.isArrive){
                    val head = vehicle.headwayCheck(edgeMap, signalWayMap)
                    edgeMap = vehicle.basicMovement(head, timestep, edgeMap)
                    stepReports = stepReports:+ new StepReport(i, vehicle.getId, vehicle.getFront, vehicle.getRear, vehicle.getSource, vehicle.getTarget,  vehicle.getEdgePath, vehicle.getCosts, vehicle.getFullPath,
                      vehicle.getEdgeIndex, vehicle.getCurrentLane, vehicle.getPosition, vehicle.getVelocity, vehicle.getCurrentLink, vehicle.getHeadSignal)
                  }else{
                    vehicle.initVehicle()
                    edgeMap = vehicle.born(edgeMap, "Baby")
                  }
                }
              })
            }
          }

          stepReports.iterator
        }
      }

      val t3 = System.currentTimeMillis()
      if(n != iteration){
        val recoverTuple = recovery(vehicleRDD.getRawSpatialRDD.rdd, signalRDD.getRawSpatialRDD.rdd, reportRDD, n*bestRepartition, numPartition)
        vehicleRDD.setRawSpatialRDD(recoverTuple._1)
        signalRDD.setRawSpatialRDD(recoverTuple._2)
        vehicleRDD.spatialPartitioning(GridType.KDBTREE, numPartition)
        edgeRDD.spatialPartitioning(vehicleRDD.getPartitioner)
        signalRDD.spatialPartitioning(vehicleRDD.getPartitioner)
      }

      val t4 = System.currentTimeMillis()
      execTime = execTime + t4 - t3

      reportHandler.writeReportJson(reportRDD, n)
    }

    logger.warn("Repartition Time: " + execTime/1000)
  }

  /**
    *
    * @param vehicleRDD the vehicle RDDs
    * @param signalRDD the signal RDDs
    * @param reportRDD the simulation report RDDs
    * @param stepCount current step
    * @param numPartition the number of partitions
    * @return signal and vehicle RDDs
    */
  def recovery(vehicleRDD: RDD[MOBILVehicle], signalRDD: RDD[TrafficLight], reportRDD: RDD[StepReport], stepCount: Int, numPartition: Int) :(RDD[MOBILVehicle], RDD[TrafficLight]) = {

    val lastReportRDD = reportRDD.filter(report => {
      report.getStep == stepCount
    }).repartition(numPartition).cache()

    //construct vehicle report
    val lastVehicle = lastReportRDD.filter(report => report.getVehicleFront != null).map(report => {
      val vehicle = new MOBILVehicle(report.getVehicleId, report.getVehicleFront, report.getTarget, report.getEdgePath, report.getCosts,report.getFullPath)
      vehicle.recoverStatus(report.getVehicleFront, report.getVehicleRear, report.getEdgeIdx, report.getCurrentLane, report.getPosition, report.getVelocity, report.getCurrentLink, report.getHeadSignal)
      vehicle
    })

    val lastSignal = lastReportRDD.filter(report => report.getSignalLocation != null).map(report => {
      val signal = new TrafficLight(report.getSignalLocation, 1, report.getWid, report.getSignalLocation)
      signal.setSignal(report.getSignal)
      signal.setTime(report.getTime)
      signal
    })

    (lastVehicle, lastSignal)
  }

  /**
    *
    * @param vehicleRDD the vehicle RDDs
    * @param signalRDD the signal RDDs
    * @param edgeRDD the edge RDDs
    * @param steps simulation steps
    * @param timestep time per step
    * @param numPartition the number of partitions
    * @return
    */
  def repartitionCriterion(vehicleRDD: SpatialRDD[MOBILVehicle], signalRDD: SpatialRDD[TrafficLight], edgeRDD: SpatialRDD[Link], steps: Int, timestep: Double, numPartition: Int): Int = {
    // 1/10 to 1/2 sample
    var bestRepartition = Int.MaxValue
    var minTime = Double.PositiveInfinity

    for(repartition <- steps/10 until steps/2 by (2*steps/25)){
      var time: Double = 0.0
      val newSteps = (steps / timestep).toInt
      val iteration = newSteps / repartition + (if (newSteps % repartition == 0) 0 else 1)
      val t1 = System.currentTimeMillis()

      for(n <- 1 to iteration){
        val reportRDD : RDD[StepReport] = vehicleRDD.spatialPartitionedRDD.sample(false, 0.01).rdd
          .zipPartitions(edgeRDD.spatialPartitionedRDD.sample(false, 0.01).rdd,
            signalRDD.spatialPartitionedRDD.sample(false, 0.01).rdd, true){

            (vehicleIt, edgeIt, signalIt) => {
              val edgeList = edgeIt.toList
              val signalList = signalIt.toList
              var stepReports = scala.List.empty[StepReport]

              var edgeMap = scala.collection.mutable.Map.empty[java.lang.Long, java.util.List[Link]].asJava
              var edge_minLat = Double.PositiveInfinity
              var edge_maxLat = Double.NegativeInfinity
              var edge_minLon = Double.PositiveInfinity
              var edge_maxLon = Double.NegativeInfinity

              for(edge <- edgeList){
                if(!edgeMap.containsKey(edge.getId)) edgeMap.put(edge.getId, new util.ArrayList[Link])
                edge_minLat = Math.min(edge.getMinLat, edge_minLat)
                edge_maxLat = Math.max(edge.getMaxLat, edge_maxLat)
                edge_minLon = Math.min(edge.getMinLon, edge_minLon)
                edge_maxLon = Math.max(edge.getMaxLon, edge_maxLon)
                edgeMap.get(edge.getId).add(edge)
              }

              //initialize traffic signals
              val signalWayMap = scala.collection.mutable.Map.empty[java.lang.Long, TrafficLight].asJava
              for(signal <- signalList){
                signalWayMap.put(signal.getWid, signal)
              }
              //initialize vehicle position
              val vehicleList = vehicleIt.toList

              vehicleList.foreach(vehicle => {
                edgeMap = vehicle.born(edgeMap, "Sync")
              })

              for(i <- repartition*(n-1)+1 to repartition*n){
                if(i <= steps){
                  for (wid <- signalWayMap.keySet().asScala){
                    val light = signalWayMap.get(wid)
                    light.next(1)
                    signalWayMap.put(wid, light)
                    stepReports = stepReports:+ new StepReport(i, light.getLocation, light.getWid, light.getSignal, light.getTime)
                  }

                  // Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath
                  vehicleList.foreach(vehicle => {
                    if(vehicle.getFront.x < edge_minLat || vehicle.getFront.x > edge_maxLat || vehicle.getFront.y < edge_minLon || vehicle.getFront.y > edge_maxLon){
                      stepReports = stepReports:+ new StepReport(i, vehicle.getId, vehicle.getFront, vehicle.getRear, vehicle.getSource, vehicle.getTarget, vehicle.getEdgePath, vehicle.getCosts, vehicle.getFullPath,
                        vehicle.getEdgeIndex, vehicle.getCurrentLane, vehicle.getPosition, vehicle.getVelocity, vehicle.getCurrentLink, vehicle.getHeadSignal)
                    }else{
                      if(!vehicle.isArrive){
                        val head = vehicle.headwayCheck(edgeMap, signalWayMap)
                        edgeMap = vehicle.basicMovement(head, timestep, edgeMap)
                        stepReports = stepReports:+ new StepReport(i, vehicle.getId, vehicle.getFront, vehicle.getRear, vehicle.getSource, vehicle.getTarget,  vehicle.getEdgePath, vehicle.getCosts, vehicle.getFullPath,
                          vehicle.getEdgeIndex, vehicle.getCurrentLane, vehicle.getPosition, vehicle.getVelocity, vehicle.getCurrentLink, vehicle.getHeadSignal)
                      }else{
                        vehicle.initVehicle()
                        edgeMap = vehicle.born(edgeMap, "Baby")
                      }
                    }
                  })
                }
              }

              stepReports.iterator
            }
          }

        if(n != iteration){
          val recoverTuple = recovery(vehicleRDD.getRawSpatialRDD.rdd, signalRDD.getRawSpatialRDD.rdd, reportRDD, n*repartition, numPartition)
          vehicleRDD.setRawSpatialRDD(recoverTuple._1)
          signalRDD.setRawSpatialRDD(recoverTuple._2)
          vehicleRDD.spatialPartitioning(GridType.KDBTREE, numPartition)
          edgeRDD.spatialPartitioning(vehicleRDD.getPartitioner)
          signalRDD.spatialPartitioning(vehicleRDD.getPartitioner)
        }
      }

      val t2 = System.currentTimeMillis()
      time = time + t2 - t1
      if(time < minTime){
        minTime = time
        bestRepartition = repartition
      }
    }

    bestRepartition
  }


}
