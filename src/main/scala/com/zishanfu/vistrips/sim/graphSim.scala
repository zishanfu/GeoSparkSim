package com.zishanfu.vistrips.sim

import org.apache.spark.graphx.Graph
import com.zishanfu.vistrips.network.Link
import com.vividsolutions.jts.geom.Point
import com.zishanfu.vistrips.model.Pair


object graphSim {
  def run(graph: Graph[Point, Link], pairs: Array[Pair]) : Unit = {
    for(i <- 0 until pairs.length ){
      
    }
  }
}