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
import com.zishanfu.vistrips.network.Route
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

class OsmGraph (sparkSession: SparkSession, path: String){
  private val LOG = LoggerFactory.getLogger(getClass);
  
  val gf = new GeometryFactory()
  val graph: Graph[Point, Link] = OsmConverter.convertToNetwork(sparkSession, path)
  val vertexRDD = new PointRDD(graph.vertices.map(r => r._2))
  vertexRDD.buildIndex(IndexType.RTREE, false)
  
  

  /**
   * @param lat
   * @param lon
   * @return
   */
  def findNearestByCoor(lat : Double, lon : Double) : Point = {
    val queryPoint = gf.createPoint(new Coordinate(lon, lat))
    var result = KNNQuery.SpatialKnnQuery(vertexRDD, queryPoint, 1, true)
    result.get(0)
  }
  

  /**
 * @param Double : latFrom
 * @param Double : lonFrom
 * @param Double : latTo
 * @param Double : lonTo
 * @return RDD[Route] : all the possible Routes
 */
def request(latFrom: Double, lonFrom : Double, latTo: Double, lonTo: Double) : RDD[Route] = {
    val source = findNearestByCoor(latFrom, lonFrom)
    val destination = findNearestByCoor(latTo, lonTo)
    val sourceId = source.getUserData.asInstanceOf[Long]
    val destinationId = destination.getUserData.asInstanceOf[Long]
    val result = ShortestPathFactory.runDijkstra(graph, sourceId, destinationId)
    LOG.info("requested the route from %s to %s".format(source, destination))
    result.filter(r => (r.legs.size > 0 && r.legs.tail == destination))
  }
  

  /**
   * @param Double : latFrom
   * @param Double : lonFrom
   * @param Double : latTo
   * @param Double : lonTo
   * @return Route : fastest route with minimum time cost
   */
  def fatestRouteRequest(latFrom: Double, lonFrom : Double, latTo: Double, lonTo: Double) : Route = {
    request(latFrom, lonFrom, latTo, lonTo).reduce((a, b) => if(a.time < b.time) a else b)
  }
  

  /**
   * @return Long : total number of vertices
   */
  def getTotalNodes() : Long = {
    graph.vertices.count()
  }
  

  /**
   * @param GeoPosition : point
   * @return GeoPosition : closest point at road network
   */
  def getClosestNode(point : GeoPosition) : GeoPosition = {
    val res = findNearestByCoor(point.getLatitude, point.getLongitude)
    new GeoPosition(res.getCoordinate.y, res.getCoordinate.x)
  }
  
}