package com.zishanfu.vistrips.map

import scala.collection.mutable.WrappedArray

import org.apache.spark.graphx.Edge
import org.apache.spark.graphx.Graph
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.geotools.referencing.CRS
import org.geotools.referencing.crs.DefaultGeocentricCRS
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.opengis.geometry.MismatchedDimensionException
import org.opengis.referencing.operation.TransformException

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Point
import com.zishanfu.vistrips.network.Link
import com.zishanfu.vistrips.tools.Distance
import org.opengis.referencing.FactoryException
import org.slf4j.LoggerFactory
import org.opengis.referencing.operation.MathTransform
import org.geotools.geometry.jts.JTS


object OsmConverter {
  
  private val LOG = LoggerFactory.getLogger(getClass)
  
  val gf = new GeometryFactory()
  val sourceCRS = DefaultGeographicCRS.WGS84;
  val targetCRS = DefaultGeocentricCRS.CARTESIAN;
  var transform : MathTransform = null 
  
  try{
    transform = CRS.findMathTransform(sourceCRS, targetCRS, true)
  }catch {
    case e: FactoryException => {
      LOG.info("Can't find CRS tranform")
    }
  }
  
  
  private def createLink(id : Long, tail :(Int, Long, Double, Double), head : (Int, Long, Double, Double), 
        speed : Int, driveDirection :Int, lanes : Int) :Link = {
        //openstreetmap <lat, lon>
        //geotools <lon, lat>
        val tailCoor = gf.createPoint(new Coordinate(tail._4, tail._3))
        tailCoor.setUserData(tail._2)
        val headCoor = gf.createPoint(new Coordinate(head._4, head._3))
        headCoor.setUserData(head._2)
        val dist = new Distance().harversineMile(headCoor, tailCoor)
        Link(id, tailCoor, headCoor, dist, speed, driveDirection, lanes)
  }
  
  private def coorParser(lat : Double, lon: Double) : Coordinate = {
    var targetCoor : Coordinate = null
    var sourceCoor = new Coordinate(lon, lat)
    try{
      targetCoor = JTS.transform(sourceCoor, null, transform);
    }catch{
      case e : MismatchedDimensionException => LOG.info("")
      case e : TransformException => LOG.info("")
    }
    targetCoor
  }
  
  def convertToNetwork(sparkSession : SparkSession, path : String) : Graph[Point, Link]= {
    val nodesPath = path + "/node.parquet"
    val waysPath = path + "/way.parquet"
    
    val nodesDF = convertNodes(sparkSession, nodesPath)
    val network = convertLinks(sparkSession, nodesDF, waysPath)
    
    val nodeDS = network._1
    val linkDS = network._2
    
    val nodesRDD = nodeDS.rdd.map(node => (node.getUserData.asInstanceOf[Long], node))
    val edgesRDD = linkDS.rdd.map(link => {
            Edge(link.getTail().getUserData.asInstanceOf[Long],
                link.getHead().getUserData.asInstanceOf[Long], 
                link)
        })

        
    val graph = Graph(nodesRDD, edgesRDD)
    println("graph processed")
    graph
  }
  
  private def convertNodes(sparkSession : SparkSession, nodesPath : String) : DataFrame = {
    val nodesDF = sparkSession.read.parquet(nodesPath)
    nodesDF.select("id", "latitude", "longitude")
  }
  
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
      val point = gf.createPoint(coorParser(r.getDouble(1), r.getDouble(2)))
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