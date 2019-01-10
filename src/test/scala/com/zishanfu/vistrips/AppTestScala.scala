package com.zishanfu.vistrips

import com.zishanfu.vistrips.map.OsmConverter
import com.zishanfu.vistrips.path.ShortestPathFactory
import org.apache.spark.graphx.PartitionStrategy

class AppTestScala extends TestBaseScala {
  describe("VisTrips test") {
    it("Test customized graph vertices and edges"){
      var graph = OsmConverter.convertToNetwork(sparkSession, resourceFolder)
      graph = graph.partitionBy(PartitionStrategy.EdgePartition2D)
      println(graph.vertices.count())
      println(graph.edges.count())
      println(graph.triplets.count())
      graph.vertices.foreach(println)
      graph.edges.foreach(println)
      graph.triplets.foreach(println)
      val routeRDD = ShortestPathFactory.runDijkstra(graph, 4347874712L, 5662664861L)
      println(routeRDD.count())
      routeRDD.filter(r => (r.legs.size > 0 && r.legs.last.getUserData.asInstanceOf[Long] == 5662664861L))
              .foreach(println) 
    }
    
//    it("Random Graph"){
//      ShortestPathFactory.runRandomGraph(sparkSession)
//    }
//    
//    it("Read CSV data") {
//      var df = sparkSession.read.format("csv").option("delimiter", "\t").option("header", "false").load(csvData)
//      df.show()
//    }
  }
}
