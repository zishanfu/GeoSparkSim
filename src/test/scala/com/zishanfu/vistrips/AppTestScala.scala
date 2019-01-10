package com.zishanfu.vistrips

import com.zishanfu.vistrips.map.OsmConverter
import com.zishanfu.vistrips.path.ShortestPathFactory
import org.apache.spark.graphx.PartitionStrategy
import com.zishanfu.vistrips.map.OsmGraph
import org.apache.spark.sql.Dataset
import org.apache.spark.rdd.RDD
import com.zishanfu.vistrips.map.OsmConverter
import com.zishanfu.vistrips.network.Link
import com.zishanfu.vistrips.map.OsmConverter
import com.vividsolutions.jts.geom.Point

class AppTestScala extends TestBaseScala {
  describe("VisTrips test") {
//    it("Test customized graph vertices and edges"){
//      var graph = OsmConverter.convertToNetwork(sparkSession, resourceFolder)
//      graph = graph.partitionBy(PartitionStrategy.EdgePartition2D)
//      println(graph.vertices.count())
//      println(graph.edges.count())
//      println(graph.triplets.count())
//      graph.vertices.foreach(println)
//      graph.edges.foreach(println)
//      graph.triplets.foreach(println)
//      val routeRDD = ShortestPathFactory.runDijkstra(graph, 4347874712L, 5662664861L)
//      println(routeRDD.count())
//      routeRDD.filter(r => (r.legs.size > 0 && r.legs.last.getUserData.asInstanceOf[Long] == 5662664861L))
//              .foreach(println) 
//    }
//    it("Road network parquet") {
//      val waysDF = sparkSession.read.parquet(waysPath).toDF("id", "tags", "nodes")
//      var res = waysDF.select(waysDF.col("id")).filter(row => row.getAs[Long](0) == 593605257L)
//      res.show()
//    }
    
//    it("Bidirectional link"){
//      val waysDF = sparkSession.read.parquet(waysPath).toDF("id", "tags", "nodes")
//      waysDF.filter(row => row.getAs[Long](0) == 216980734L).show()
//    }
    
//    it("Road network"){
//      val nodesDF = OsmConverter.convertNodes(sparkSession, nodesPath)
//      val network = OsmConverter.convertLinks(sparkSession, nodesDF, waysPath)
//    
//      val nodeDS : RDD[Point] = network._1.rdd
//      val linkDS : RDD[Link] = network._2.rdd
//      
//      linkDS.filter(link => (link.getTail().getUserData.asInstanceOf[Long] == 2262996383L || 
//                link.getHead().getUserData.asInstanceOf[Long] == 2262996383L)).foreach(println)
//    }
    
    it("Map fatest route test"){
      //33.410065, -111.920412 2262996384
      //33.406198, -111.939376 5662664860
      var graph = new OsmGraph(sparkSession, hdfs)
      val from = graph.findNearestByCoor(33.410065, -111.920412)
      val to = graph.findNearestByCoor(33.406198, -111.939376)
      println(from)
      println(to)

      val route = graph.fatestRouteRequest(33.410065, -111.920412, 33.406198, -111.939376)
      route.legs.foreach(println)
      
//      var graph = OsmConverter.convertToNetwork(sparkSession, hdfs)
//      val routeRDD = ShortestPathFactory.runDijkstra(graph, 2262996384L, 5662664860L)
//      println(routeRDD.count())
//      routeRDD.filter(r => (r.legs.size > 0 && r.legs.last.getUserData.asInstanceOf[Long] == 5662664860L))
//              .foreach(println) 

      
    }
//    
//    it("Read CSV data") {
//      var df = sparkSession.read.format("csv").option("delimiter", "\t").option("header", "false").load(csvData)
//      df.show()
//    }
  }
}
