package com.zishanfu.vistrips.map

import com.vividsolutions.jts.geom.{Coordinate, GeometryFactory, Point}
import com.zishanfu.vistrips.network.Link
import com.zishanfu.vistrips.tools.Distance
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

import scala.collection.mutable.WrappedArray


object OsmConverter {
  
  private val LOG = LoggerFactory.getLogger(getClass)
  
  val gf = new GeometryFactory()
  
  
    /**
   * @param id
   * @param tail
   * @param head
   * @param speed
   * @param driveDirection
   * @param lanes
   * @return
   */
  private def createLink(id : Long, tail :(Int, Long, Double, Double), head : (Int, Long, Double, Double), 
        speed : Int, driveDirection :Int, lanes : Int) :Link = {
        val tailCoor = gf.createPoint(coorParserByCRS(tail._3, tail._4, DefaultGeographicCRS.WGS84))
        tailCoor.setUserData(tail._2)
        val headCoor = gf.createPoint(coorParserByCRS(head._3, head._4, DefaultGeographicCRS.WGS84))
        headCoor.setUserData(head._2)
        val dist = new Distance().harversineMile(headCoor, tailCoor)
        Link(id, tailCoor, headCoor, dist, speed, driveDirection, lanes)
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
    
    val nodeDS : RDD[Point] = network._1.rdd
    val linkDS : RDD[Link] = network._2.rdd

    // Set each partition of nodeDS RDD contain 1000 links by default. We may need a smarter choice here to determine the partition size.
    val partitionNum= math.ceil(nodeDS.count()/1000.0).toInt

    val nodesRDD = nodeDS.map(node => (node.getUserData.asInstanceOf[Long], node)).coalesce(partitionNum, false)
    val edgesRDD = linkDS.map(link => {
            Edge(link.getTail().getUserData.asInstanceOf[Long],
                link.getHead().getUserData.asInstanceOf[Long], 
                link)
        }).coalesce(partitionNum, false)
        

    val graph = Graph(nodesRDD, edgesRDD)
    LOG.info("Constructed graph with %s vertices %s edges".format(nodesRDD.count(), edgesRDD.count()))
    graph
  }
  
  private def convertNodes(sparkSession : SparkSession, nodesPath : String) : DataFrame = {
    val nodesDF = sparkSession.read.parquet(nodesPath)
    nodesDF.select("id", "latitude", "longitude")
  }
  
  /**
   * @param SparkSession sparkSession
   * @param DataFrame nodeDF
   * @param String waysPath
   * @return Tuple (Dataset[Point], Dataset[Link])
   */
  private def convertLinks(sparkSession : SparkSession, nodeDF : DataFrame, waysPath : String) = {
    val linkEncoder = Encoders.kryo[Link]
    val PointEncoder = Encoders.kryo[Point]
    
    val defaultSpeed = 40 //40mph
    val waysDF = sparkSession.read.parquet(waysPath).toDF("id", "tags", "nodes")

    val wayNodesDF = waysDF.select(col("id").as("wayId"), col("tags"), explode(col("nodes")).as("indexedNode"))
        .withColumn("linkId", monotonically_increasing_id())
        
            
    var nodeLinkJoinDF = nodeDF.join(wayNodesDF, col("indexedNode.nodeId") === nodeDF("id"))
        nodeLinkJoinDF.cache()
        
    var nodesInLinksDF = nodeLinkJoinDF.select(col("indexedNode.nodeId").as("id"), col("latitude"), col("longitude")).dropDuplicates()
            
    val wayDF = nodeLinkJoinDF.groupBy(col("wayId"), col("tags"))
        .agg(collect_list(struct(col("indexedNode.index"), col("indexedNode.nodeId"), col("latitude"), col("longitude"))).as("nodes")
        , collect_list(col("linkId")).as("linkIds"))
        
    
    
    var linkDS :Dataset[Link] = wayDF.flatMap((row : Row) => { 
      val id = row.getAs[Long](0)
      val tags = row.getAs[WrappedArray[Row]](1)
      var nodes = row.getAs[WrappedArray[Row]](2).map(r => (r.getInt(0), r.getAs[Long](1), r.getAs[Double](2), r.getAs[Double](3))).array
                      .sortBy(x => x._1)
      
      var links : List[Link] = List.empty[Link]
      var tagsMap = Map.empty[String, String]

      tagsMap = tags.map(r => new String(r.getAs[Array[Byte]]("key")) -> new String(r.getAs[Array[Byte]]("value"))).toMap
      
      var speed = defaultSpeed
      var maxSpeed = tagsMap.get("maxspeed")
      if(!maxSpeed.isEmpty) speed = maxSpeed.get.substring(0, 2).toInt
      
      var isOneWay = tagsMap.getOrElse("oneway", "no") == "yes"
      isOneWay = tagsMap.getOrElse("junction", "default") == "roundabout"
      val lanes = if(tagsMap.contains("lanes")) tagsMap.get("lanes").get.toInt else 1
      isOneWay = if(lanes == 1) true else false

      val driveDirection = if(isOneWay) 1 else 2

      
      var linkIds = row.getAs[WrappedArray[Long]](3).toArray
      
      for (i <- 0 until nodes.length - 1) {
        var tail = nodes(i)
        var head = nodes(i + 1)
        
        links = links :+ createLink(id, tail, head, speed, driveDirection, lanes)
        
      }
            
      links
            
   })(linkEncoder)

        
   var nodeDS = nodesInLinksDF.map((r:Row) =>{
      val point = gf.createPoint(coorParserByCRS(r.getDouble(1), r.getDouble(2), DefaultGeographicCRS.WGS84))
      point.setUserData(r.getLong(0))
      point
   })(PointEncoder)
   
   linkDS = linkDS.flatMap(link => {
     if(link.getDrivingDirection() == 1){
        List(link)
      }else{
        List(
          Link(link.getId(), link.getHead(), link.getTail(), link.getDistance(), link.getSpeed(), 1, link.getLanes()/2),
          Link(link.getId(), link.getTail(), link.getHead(), link.getDistance(), link.getSpeed(), 1, link.getLanes()/2)
        )
      }
   })(linkEncoder)
        
   
   (nodeDS, linkDS)
     

  }
  
  
}