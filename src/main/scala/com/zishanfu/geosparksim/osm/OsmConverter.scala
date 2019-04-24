package com.zishanfu.geosparksim.osm

import com.vividsolutions.jts.algorithm.Angle
import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import com.zishanfu.geosparksim.Model.{Intersect, Link, TrafficLight}
import com.zishanfu.geosparksim.model.SegmentLink
import com.zishanfu.geosparksim.model.SegmentNode
import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.geotools.geometry.jts.JTS
import org.geotools.referencing.CRS
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.opengis.geometry.MismatchedDimensionException
import org.opengis.referencing.FactoryException
import org.opengis.referencing.operation.{MathTransform, TransformException}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.WrappedArray


case class RoadNetwork(nodes: RDD[Point], links: RDD[Link], lights: RDD[TrafficLight], intersects: RDD[Intersect])

object OsmConverter {
  
  private val LOG = LoggerFactory.getLogger(getClass)
  private val lane_width = Define.lane_width
  val gf = new GeometryFactory()

  /**
   * @param SparkSession: sparkSession
   * @param String path
   * @return Graph[Point, Link] graph
   */
  def convertToNetwork(sparkSession : SparkSession, path : String) : Graph[Point, Link]= {
    val nodesPath = path + "/node.parquet"
    val waysPath = path + "/way.parquet"
    
    val nodesDF = convertNodes(sparkSession, nodesPath)
    val network = convertLinks(sparkSession, nodesDF, waysPath)
    
    val nodeDS : RDD[Point] = network._1
    val linkDS : RDD[Link] = network._2

    // Set each partition of nodeDS RDD contain 1000 links by default. We may need a smarter choice here to determine the partition size.
    //val partitionNum= math.ceil(nodeDS.count()/1000.0).toInt
    val partitionNum= 8

    val nodesRDD = nodeDS.map(node => (node.getUserData.asInstanceOf[Long], node)).coalesce(partitionNum, false)
    val edgesRDD = linkDS.map(link => {
            Edge(link.getHead.id, link.getTail.id, link)
        }).coalesce(partitionNum, false)

    val graph = Graph(nodesRDD, edgesRDD)
    LOG.info("Constructed graph with %s vertices %s edges".format(nodesRDD.count(), edgesRDD.count()))
    graph
  }


  def convertToRoadNetwork(sparkSession : SparkSession, path : String): RoadNetwork = {
    val nodesPath = path + "/node.parquet"
    val waysPath = path + "/way.parquet"

    val nodesDF = convertNodes(sparkSession, nodesPath)
    val network = convertLinks(sparkSession, nodesDF, waysPath)

    RoadNetwork(network._1, network._2, network._3, network._4)
  }

  def convertNodes(sparkSession : SparkSession, nodesPath : String) : DataFrame = {
    var nodesDF = sparkSession.read.parquet(nodesPath)
    import sparkSession.implicits._
    nodesDF = nodesDF.select("id", "latitude", "longitude", "tags")

    val filteredLights = nodesDF.filter(row => {
      val tags = row.getAs[WrappedArray[Row]](3)
      var tagsMap = Map.empty[String, String]
      tagsMap = tags.map(r => new String(r.getAs[Array[Byte]]("key")) -> new String(r.getAs[Array[Byte]]("value"))).toMap
      val highWay = tagsMap.get("highway")
      if(highWay.isDefined) highWay.get == "traffic_signals" else false
    })

    val lights = filteredLights.select("id", "tags").map( row => {
      val id = row.getAs[Long](0)
      val tags = row.getAs[WrappedArray[Row]](1)
      var tagsMap = Map.empty[String, String]
      tagsMap = tags.map(r => new String(r.getAs[Array[Byte]]("key")) -> new String(r.getAs[Array[Byte]]("value"))).toMap
      (id, tagsMap)
    }).toDF("lid", "signals")

    val newNodes = nodesDF.select("id", "latitude", "longitude")
    val newLightNodes = newNodes.join(lights, col("lid") === newNodes("id"), "full").select("id", "latitude", "longitude", "signals")

    newLightNodes
  }
  
  /**
   * @param SparkSession sparkSession
   * @param DataFrame nodeDF
   * @param String waysPath
   * @return Tuple (Dataset[Point], Dataset[Link])
   */
  def convertLinks(sparkSession : SparkSession, nodeDF : DataFrame, waysPath : String) = {
    val segmentEncoder = Encoders.kryo[SegmentLink]

    val defaultSpeed = 40 //40mph
    val waysDF = sparkSession.read.parquet(waysPath).toDF("id", "tags", "nodes")

    val wayNodesDF = waysDF.select(col("id").as("wayId"), col("tags"), explode(col("nodes")).as("indexedNode"))
        .withColumn("segmentId", monotonically_increasing_id())

    var nodeSegmentJoinDF = nodeDF.join(wayNodesDF, col("indexedNode.nodeId") === nodeDF("id"))
        nodeSegmentJoinDF.cache()
    val nodeCount = nodeSegmentJoinDF.groupBy(col("indexedNode.nodeId").as("id"), col("latitude"), col("longitude")).count()

    val intersectNode = nodeCount.withColumnRenamed("count", "n")
      .filter("n > 2")
      .select(col("id").as("Iid"), col("latitude").as("Llatitude"), col("longitude").as("Llongitude"), col("n"))
      .dropDuplicates()

    nodeSegmentJoinDF = intersectNode.join(nodeSegmentJoinDF, col("indexedNode.nodeId") === intersectNode("Iid"), "full")

      //.select("indexedNode.nodeId", "latitude", "longitude", "signals", "n")
    //val signal_count = nodeSegmentJoinDF.filter("signals is not null").count()
    //val intersect_count = nodeSegmentJoinDF.filter("signals is null and n is not null").count()

    val nodesInSegmentsDF = nodeSegmentJoinDF.select(col("indexedNode.nodeId").as("id"), col("latitude"), col("longitude")).dropDuplicates()

    val wayDF = nodeSegmentJoinDF.groupBy(col("wayId"), col("tags"))
        .agg(collect_list(struct(col("indexedNode.index"), col("indexedNode"), col("latitude"), col("longitude"), col("signals"), col("n"))).as("nodes")
        , collect_list(col("segmentId")).as("segmentIds"))


    var segmentRDD :RDD[SegmentLink] = wayDF.flatMap((row : Row) => {
      val id = row.getAs[Long](0)
      val tags = row.getAs[mutable.WrappedArray[Row]](1)

      val nodes = row.getAs[mutable.WrappedArray[Row]](2).map(r => {
        val id_arr = r.getAs[Row](1).toSeq.toArray
        (r.getInt(0), id_arr(1).asInstanceOf[Long], r.getAs[Double](2), r.getAs[Double](3), r.getAs[Map[String, String]](4), r.getAs[String](5))
      }).array.sortBy(x => x._1)
      
      var segments : List[SegmentLink] = List.empty[SegmentLink]
      var tagsMap = Map.empty[String, String]

      tagsMap = tags.map(r => new String(r.getAs[Array[Byte]]("key")) -> new String(r.getAs[Array[Byte]]("value"))).toMap
      
      var speed = defaultSpeed
      val maxSpeed = tagsMap.get("maxspeed")
      
      try{
        if(maxSpeed.isDefined && maxSpeed.get.length() > 2) speed = maxSpeed.get.substring(0, 2).toInt
      }catch{
        case _: NumberFormatException => speed = defaultSpeed
      }

      var isOneWay = if(tagsMap.contains("oneway")) tagsMap("oneway").equals("yes") else false
      val lanes = if(tagsMap.contains("lanes")) tagsMap("lanes").toInt else 2
      isOneWay = if(lanes == 1) true else isOneWay

      val driveDirection = if(isOneWay) 1 else 2
      var segmentIds = row.getAs[mutable.WrappedArray[Long]](3).toArray
      
      for (i <- 0 until nodes.length - 1) {
        val head = nodes(i)
        val tail = nodes(i + 1)
        segments = segments :+ createSegment(id, head, tail, speed, driveDirection, lanes)
      }

      segments
            
   })(segmentEncoder).rdd


   val nodeRDD : RDD[Point] = nodesInSegmentsDF.map((r:Row) =>{
      val point = gf.createPoint(coorParserByCRS(r.getDouble(1), r.getDouble(2), DefaultGeographicCRS.WGS84))
      point.setUserData(r.getLong(0))
      point
   })(Define.PointEncoder).rdd

    // 0 -> forward
    // 1 -> backward
    val directSegmentRDD = segmentRDD.flatMap(segment => {
     if(segment.driveDirection == 1){
        List(segment)
      }else{
        List(
          SegmentLink(segment.id, segment.head, segment.tail, segment.distance, segment.speed, 1, segment.lanes/2),
          SegmentLink(segment.id, segment.tail, segment.head, segment.distance, segment.speed, 0, segment.lanes/2)
        )
      }
   })

    val linkRDD : RDD[Link]= segmentRDD.map(segment => {
      val angle = Angle.angle(segment.head.coordinate, segment.tail.coordinate)
      val lanes : Int = segment.lanes
      var laneArray: String = ""
      val head : Coordinate = segment.head.coordinate
      val tail : Coordinate = segment.tail.coordinate
      for(i <- 0 until lanes){
        laneArray += "#" + defineLane(i+1, head, tail, angle)
      }
      val path : Array[Coordinate]= Array(head, tail)
      new Link(segment.id, segment.head, segment.tail, segment.distance, segment.speed, segment.driveDirection, lanes, angle, laneArray, path)
      //(segment.id, segment.head, segment.tail, segment.distance, segment.speed, direction, lanes, angle, laneArray)
    })

    //(Coordinate coordinate, int SRID, long wid, Coordinate location)
    val signalRDD : RDD[TrafficLight] = linkRDD.filter(link => {
      val direction = if(link.getDriveDirection == 1) true else false
      direction && (link.getHead.signal || link.getTail.signal)
      //link.head.signal || link.tail.signal
    }).map(link => {
      val headNode = link.getHead
      val tailNode = link.getTail
      val reverse_angle = Angle.angle(tailNode.coordinate, headNode.coordinate)
      val node = if(headNode.signal) headNode else tailNode
      val location = calculateLocation(node.coordinate, reverse_angle, 1)
      new TrafficLight(node.coordinate, node.id.toInt, link.getId, location)
    })


    val intersectRDD = linkRDD.filter(link => {
      !link.getHead.signal && !link.getTail.signal && (link.getHead.intersect || link.getTail.intersect)
    }).map(link => {
      val headNode = link.getHead
      val tailNode = link.getTail
      val node = if(headNode.intersect) headNode else tailNode
      new Intersect(node.coordinate, node.id.toInt, link.getId)
    })

   (nodeRDD, linkRDD, signalRDD, intersectRDD)
  }

  /**
    * @param id
    * @param tail
    * @param head
    * @param speed
    * @param driveDirection
    * @param lanes
    * @return
    */
  private def createSegment(id : Long, head :(Int, Long, Double, Double, Map[String, String], String), tail : (Int, Long, Double, Double, Map[String, String], String),
                         speed : Int, driveDirection :Int, lanes : Int) :SegmentLink = {
    val tailNode = formatSegmentNode(tail)
    val headNode = formatSegmentNode(head)

    val dist = Haversine.haversine(tailNode.coordinate.x, tailNode.coordinate.y, headNode.coordinate.x, headNode.coordinate.y)
    SegmentLink(id, headNode, tailNode, dist, speed, driveDirection, lanes)
  }


  private def formatSegmentNode(node : (Int, Long, Double, Double, Map[String, String], String)) : SegmentNode = {
    val map : Map[String, String] = node._5
    val signal : Boolean= if(map != null && map("highway").equals("traffic_signals")) true else false

    val intersect : Boolean = if(node._6 != null) true else false
    SegmentNode(node._2, new Coordinate(node._3, node._4), signal, intersect)
  }

  //  0      1          2           3            4
  // id | headline | tailline | headcenter | tailcenter
  private def defineLane(id: Int, head: Coordinate, tail: Coordinate, angle: Double): String = {
    String.format("%s|%s|%s|%s|%s", id.toString, calculateCoordinate(head, angle, 1, id), calculateCoordinate(tail, angle, 1, id),
      calculateCoordinate(head, angle, 0.5, id), calculateCoordinate(tail, angle, 0.5, id))
  }

  private def calculateCoordinate(point: Coordinate, angle: Double, perc: Double, id: Int): String = {
    val nid = if(id == 1) 0.5 else id-1 + 0.5
    val pid = if(perc == 0.5) nid else perc*id
    String.format("%s,%s", (point.x - Math.sin(angle)*lane_width*pid).toString, (point.y + Math.cos(angle)*lane_width*pid).toString)
  }

  private def calculateLocation(point: Coordinate, angle: Double, id: Int): Coordinate ={
    new Coordinate(point.x + Math.cos(angle)*lane_width*id, point.y + Math.sin(angle)*lane_width*id)
  }

  /**
    * @param Double lat
    * @param Double lon
    * @param DefaultGeographicCRS crs
    * @return Coordinate coordinate
    */

  def coorParserByCRS(lat : Double, lon: Double, crs: DefaultGeographicCRS) : Coordinate = {
    val sourceCRS = DefaultGeographicCRS.WGS84;
    val targetCRS = crs;
    var transform : MathTransform = null
    var targetCoor : Coordinate = null
    var sourceCoor = new Coordinate(lon, lat)

    if(sourceCRS == targetCRS) targetCoor

    try{
      transform = CRS.findMathTransform(sourceCRS, targetCRS, true)
    }catch {
      case e: FactoryException => {
        LOG.info("Can't find CRS tranform")
      }
    }

    try{
      targetCoor = JTS.transform(sourceCoor, null, transform);
    }catch{
      case e : MismatchedDimensionException => LOG.info("")
      case e : TransformException => LOG.info("")
    }
    targetCoor
  }
  
  
}