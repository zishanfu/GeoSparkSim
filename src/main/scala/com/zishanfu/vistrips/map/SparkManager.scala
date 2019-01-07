package com.zishanfu.vistrips.map

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.graphx.Graph
import com.zishanfu.vistrips.network.Link
import com.vividsolutions.jts.geom.Point
import com.zishanfu.vistrips.path.ShortestPathFactory

object SparkManager {
  def main(args: Array[String]) : Unit = {
    run("default")
  }
  
  //source: 33.410728, -111.921009
  //destination: 33.407444, -111.934596
  
  //source: 5668170615
  //destination: 4347874712
  
  def run(path: String) : Unit = {
    val conf = new SparkConf().setAppName("OSMSpark").setMaster("local")
    val sparkSession = SparkSession.builder().config(conf)
                      .config("spark.serializer", classOf[KryoSerializer].getName)
                      .config("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)
                      .getOrCreate()
//    val graph = new OsmGraph(sparkSession, path)
//    graph.request(33.410728, -111.921009, 33.407444, -111.934596)
    ShortestPathFactory.runRandomGraph(sparkSession)
  }
}