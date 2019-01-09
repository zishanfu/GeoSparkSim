package com.zishanfu.vistrips

import com.zishanfu.vistrips.map.OsmConverter
import com.zishanfu.vistrips.path.ShortestPathFactory

class AppTestScala extends TestBaseScala {
  describe("VisTrips test") {
    it("Test customized graph vertices and edges"){
      val graph = OsmConverter.convertToNetwork(sparkSession, resourceFolder)
      println(graph.vertices.count())
      println(graph.edges.count())
      graph.vertices.foreach(println)
      graph.edges.foreach(println)
      val routeRDD = ShortestPathFactory.runDijkstra(graph, 4347874712L, 5662664861L)
      println(routeRDD.count())
      
    }
    it("Read CSV data") {
      var df = sparkSession.read.format("csv").option("delimiter", "\t").option("header", "false").load(csvData)
      df.show()
    }
  }
}
