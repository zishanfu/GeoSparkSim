package com.zishanfu.vistrips.path

import org.apache.spark.graphx.EdgeDirection
import org.apache.spark.graphx.EdgeTriplet
import org.apache.spark.graphx.Pregel
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.Graph
import com.zishanfu.vistrips.network.Route
import com.zishanfu.vistrips.network.Link
import com.vividsolutions.jts.geom.Point

object BulkShortestPath extends Serializable{
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
}