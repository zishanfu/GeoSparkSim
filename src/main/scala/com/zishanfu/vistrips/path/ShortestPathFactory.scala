package com.zishanfu.vistrips.path

import org.apache.spark.graphx.Graph
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.sql.Row

import com.vividsolutions.jts.geom.Point
import com.zishanfu.vistrips.network.Link
import org.apache.spark.graphx.Pregel
import org.apache.spark.graphx.EdgeDirection
import org.apache.spark.graphx.EdgeTriplet
import org.apache.spark.sql.SparkSession
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate
import org.apache.spark.sql.Encoders
import org.apache.spark.graphx.Edge
import org.apache.spark.graphx.VertexRDD
import org.apache.spark.rdd.RDD
import com.zishanfu.vistrips.network.Route

object ShortestPathFactory {
//Node: (id, lat, lon)
//A (4347874712, 33.4074225, -111.9389839)
//B (6058475294,  33.4074298, -111.9385580)
//C (41883108, 33.4074352, -111.9383137)
//
//D (5662664854, 33.4071662, -111.9383377)
//E (5662664855,  33.4069101, -111.9383463)
//
//F(5662664858, 33.4068765, -111.9384113)
//G(5662664859, 33.4064041, -111.9384314)
//
//H(5662664860, 33.4063666, -111.9393425)
//I(5662664861, 33.4063538, -111.9398013)
//
//J(3983483491, 33.4067486, -111.9398081)
  val gf = new GeometryFactory()
  
  def createPoint(lon : Double, lat: Double, id: Long) : Point = {
    val p = gf.createPoint(new Coordinate(lon, lat))
    p.setUserData(id)
    p
  }
  
  def runRandomGraph(sparkSession : SparkSession) : Unit = {
    
    val sc = sparkSession.sparkContext
    val map = Map("A" -> createPoint(-111.9389839, 33.4074225, 4347874712L),
                  "B" -> createPoint(-111.9385580, 33.4074298, 6058475294L),
                  "C" -> createPoint(-111.9383137, 33.4074352, 41883108L),
                  "D" -> createPoint(-111.9383377, 33.4071662, 5662664854L),
                  "E" -> createPoint(-111.9383463, 33.4069101, 5662664855L),
                  "F" -> createPoint(-111.9384113, 33.4068765, 5662664858L),
                  "G" -> createPoint(-111.9384314, 33.4064041, 5662664859L),
                  "H" -> createPoint(-111.9393425, 33.4063666, 5662664860L),
                  "I" -> createPoint(-111.9398013, 33.4063538, 5662664861L),
                  "J" -> createPoint(-111.9398081, 33.4067486, 3983483491L))
    val nodeRDD = sc.parallelize(map.values.toSeq)
    
    val linkDS = Seq( Link(1L, map.getOrElse("C", null).asInstanceOf[Point], 
                                map.getOrElse("A", null).asInstanceOf[Point] , 0.01958306584828093, 40, 2, 2), //0.1
                      Link(2L, map.getOrElse("E", null).asInstanceOf[Point], 
                                map.getOrElse("C", null).asInstanceOf[Point] , 0.050637545877608803, 40, 2, 2), //0.05
                      Link(3L, map.getOrElse("G", null).asInstanceOf[Point], 
                                map.getOrElse("E", null).asInstanceOf[Point] , 0.080701717430328744, 40, 2, 2), //0.08
                      Link(4L, map.getOrElse("I", null).asInstanceOf[Point], 
                                map.getOrElse("G", null).asInstanceOf[Point] , 0.04440963718096148, 40, 2, 2), //0.04
                      Link(5L, map.getOrElse("J", null).asInstanceOf[Point], 
                                map.getOrElse("I", null).asInstanceOf[Point] , 0.01266020623253597, 40, 2, 2),//0.01
                      Link(6L, map.getOrElse("A", null).asInstanceOf[Point], 
                                map.getOrElse("J", null).asInstanceOf[Point] , 0.052614319867506025, 40, 2, 2), //0.05
                      Link(7L, map.getOrElse("J", null).asInstanceOf[Point], 
                                map.getOrElse("D", null).asInstanceOf[Point] , 0.08456997082524347, 40, 2, 2)) //0.08
    val linkRDD = sc.parallelize(linkDS).flatMap(link => {
     if(link.getDrivingDirection() == 1){
        List(link)
      }else{
        List(
          Link(link.getId(), link.getHead(), link.getTail(), link.getDistance(), link.getSpeed(), 1, link.getLanes()/2),
          Link(link.getId(), link.getTail(), link.getHead(), link.getDistance(), link.getSpeed(), 1, link.getLanes()/2)
        )
      }
   })
   
    val nodesRDD = nodeRDD.map(node => (node.getUserData.asInstanceOf[Long], node))
    val edgesRDD = linkRDD.map(link => {
            Edge(link.getTail().getUserData.asInstanceOf[Long],
                link.getHead().getUserData.asInstanceOf[Long], 
                link)
        })
        
    val graph = Graph(nodesRDD, edgesRDD)
    val A = map.getOrElse("A", null).asInstanceOf[Point]
    val G = map.getOrElse("G", null).asInstanceOf[Point]
    println(graph.vertices.count())
    println(graph.edges.count())
    println(graph.triplets.count())
    graph.vertices.foreach(println)
    graph.edges.foreach(println)
    graph.triplets.foreach(println)
    val result = runDijkstra(graph, A.getUserData.asInstanceOf[Long], G.getUserData.asInstanceOf[Long])
    
    //runBuiltin(graph, A.getUserData.asInstanceOf[Long], G.getUserData.asInstanceOf[Long])
    val swapMap = map.map(_.swap)
    
    result.filter(_.legs contains map.getOrElse("G", null)).map(row => {
      val cost = row.distance
      val list = row.legs
      val list2 = list.map(e => swapMap.getOrElse(e, null))
      (cost, list2)
    }).foreach(println)

  }
  
    def vertexProgram(id: Long, vertex: Route, msg: Route): Route = {
      if(vertex.time < msg.time){
          vertex
       }else{
          msg
       }
    }
    
    def sendMessage(triplet: EdgeTriplet[Route, Link]): Iterator[(Long, Route)] = {
      val source = triplet.srcAttr
      val destination = triplet.dstAttr
      val attr = triplet.attr
      val timeWeight = attr.distance / attr.speed
      val timeCost = source.time + timeWeight
      val distanceCost = source.distance + attr.distance
      if(timeCost < destination.time){
        var list = source.legs
        list = list :+ triplet.attr.getHead()
        Iterator((triplet.dstId, new Route(destination.getDestination(), distanceCost, timeCost, list)))
      }else{
        Iterator.empty
      }
    }
    
    def messageCombiner(msg1 : Route, msg2: Route) : Route = {
      if(msg1.time < msg2.time) msg1 else msg2
    }
   
  
  def runDijkstra(graph : Graph[Point, Link],  source : Long , destination : Long) : RDD[Route] = {
    //(distance, time, list of points)
    val initialMessage : Route = new Route(destination, Double.PositiveInfinity, Double.PositiveInfinity, List())
    val spGraph = graph.mapVertices { (vid, vertex) => 
      if(vid == source) new Route (destination, 0.0, 0.0, List(vertex)) else new Route(destination, Double.PositiveInfinity, Double.PositiveInfinity, List())}
                      
    val count = spGraph.vertices.count().toInt
    println(count)
    val pregel = Pregel(spGraph, initialMessage, count, EdgeDirection.Out)(vertexProgram, sendMessage, messageCombiner)
    pregel.vertices.map(_._2)
  }
  
  def runBuiltin(graph: Graph[Point, Link], sourceId: Long, destinationId: Long) = {
        
        val spResult = ShortestPaths.run(graph, Seq(sourceId, destinationId))
        //Map(id: long -> landmarks count)
        spResult.vertices.map(_._2).collect.foreach(println)
    }
}