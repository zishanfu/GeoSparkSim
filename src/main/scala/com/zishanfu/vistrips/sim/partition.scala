package com.zishanfu.vistrips.sim

import org.apache.spark.rdd.RDD
import org.datasyslab.geospark.spatialRDD.LineStringRDD
import org.datasyslab.geospark.enums.GridType
import com.vividsolutions.jts.geom.LineString



object partition {
  def networkRun() : Unit = {
    
  }
  
  def trajectoriesRun(tracjectories : RDD[LineString]) : Unit = {
    val trajRDD = new LineStringRDD(tracjectories)
    trajRDD.spatialPartitioning(GridType.QUADTREE)
  }
}