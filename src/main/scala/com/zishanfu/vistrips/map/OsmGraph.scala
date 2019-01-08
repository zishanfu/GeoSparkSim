package com.zishanfu.vistrips.map

import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx.Graph
import com.zishanfu.vistrips.network.Link
import com.vividsolutions.jts.geom.Point
import com.zishanfu.vistrips.path.ShortestPathFactory
import org.apache.spark.graphx.VertexRDD
import org.datasyslab.geospark.spatialOperator.KNNQuery
import org.datasyslab.geospark.spatialRDD.PointRDD
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate
import org.datasyslab.geospark.enums.IndexType
import org.jxmapviewer.viewer.GeoPosition
import com.vividsolutions.jts.geom.LineString

class OsmGraph (sparkSession: SparkSession, path: String){
  
  val gf = new GeometryFactory()
  val graph: Graph[Point, Link] = OsmConverter.convertToNetwork(sparkSession, path)
  val vertexRDD = new PointRDD(graph.vertices.map(r => r._2))
  vertexRDD.buildIndex(IndexType.RTREE, false)
  
  
  def findNearestByCoor(lon : Double, lat : Double) : Point = {
    val queryPoint = gf.createPoint(new Coordinate(lat, lon))
    var result = KNNQuery.SpatialKnnQuery(vertexRDD, queryPoint, 1, true)
    result.get(0)
  }
  
  //double latFrom, double lonFrom, double latTo, double lonTo
  def request(latFrom: Double, lonFrom : Double, latTo: Double, lonTo: Double) : (Long, Long) = {
    val source = findNearestByCoor(latFrom, lonFrom)
    val destination = findNearestByCoor(latTo, lonTo)
    val sourceId = source.getUserData.asInstanceOf[Long]
    val destinationId = destination.getUserData.asInstanceOf[Long]
    (sourceId, destinationId)
    
    
  }
  
  //routeRequest
  //return a list of point
  def routeRequest(latFrom: Double, lonFrom : Double, latTo: Double, lonTo: Double) : LineString = {
    val tuple = request(latFrom, lonFrom, latTo, lonTo)
    val rdd = ShortestPathFactory.runDijkstra(graph, tuple._1, tuple._2)
    rdd.map(row => {
      row._2
    })
  }
  
  //routeDistCompute
  //return distance
  def routeDistCompute() : Unit = {
    
  }
  
  //routeCostCompute
  //return estimate time cost
  def routeCostCompute() : Unit = {
    
  }
  
  //totalNodes
  def getTotalNodes() : Long = {
    graph.vertices.count()
  }
  
  //getCoorById
  def getCoorById() : Unit = {
    
  }
  
  //getClosestNode
  def getClosestNode(point : GeoPosition) : GeoPosition = {
    val res = findNearestByCoor(point.getLongitude, point.getLatitude)
    new GeoPosition(res.getCoordinate.y, res.getCoordinate.x)
  }
  
}