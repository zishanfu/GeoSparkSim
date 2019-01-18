package com.zishanfu.vistrips


import com.zishanfu.vistrips.tools.FileOps
import com.zishanfu.vistrips.map.CountyPop
import com.zishanfu.vistrips.map.GraphInit
import org.jxmapviewer.viewer.GeoPosition
import com.zishanfu.vistrips.tools.Distance
import com.zishanfu.vistrips.model.Pair
import org.apache.spark.sql.Row
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.DoubleType
import com.vividsolutions.jts.geom.LineString
import com.zishanfu.vistrips.tools.Interpolate
import org.datasyslab.geospark.spatialRDD.SpatialRDD
import org.datasyslab.geospark.enums.GridType


class AppTestScala extends TestBaseScala {
  
  describe("Routes"){
    it("generation test"){
      // p1: [33.414964957503585, -111.94467544555664], p2: [33.39031619194356, -111.89120292663574]
    	val geo1 = new GeoPosition(33.414964957503585, -111.94467544555664)
    	val geo2 = new GeoPosition(33.39031619194356, -111.89120292663574)
    	val selectedType = "DSO"
    	val maxLen = new Distance().euclidean(geo1, geo2) / 10; 
//    	val newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen)
//		  val newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen)
//		  
//		  val osmloader = new OsmLoader(newGeo1, newGeo2)
//		  val path = osmloader.download()
    	
      val graphhopper = new GraphInit(resourceFolder + "/vistrips/2019-01-17T11:01:34Z.osm")
      val nums = 1000
      //val tg = new TripsGeneration(geo1, geo2, graphhopper, maxLen)
      //val pairs = tg.computePairs(nums, selectedType)
//      var rdd = sparkSession.sparkContext.parallelize(pairs.toSeq).filter(p => p != null).map(
//          pair => Row(pair.getSourceCoor, pair.getDestCoor, pair.getDistance, pair.getTime, pair.getRoute))
//      var rddCopy = rdd
//      for(i <- 1 until 100){
//        rddCopy = rddCopy.union(rdd)
//      }
//      
//      val t1 = System.nanoTime
//      
//    	val newRDD = rddCopy.map(row => {
//    	  val lsRoute = row.getAs[LineString](4)
//    	  val time = row.getAs[Long](3)
//    	  val distance = row.getAs[Double](2)
//    	  //val routeInSec = new Interpolate().routeInterpolate(lsRoute, time, distance);
//    	  val routeInSec = new Interpolate().routeInterpolateBySec(lsRoute, time, distance, 0.4);
//    	  routeInSec
//    	})
      
      
//      val t1 = System.nanoTime
//      
//      val slot = 10
//      val steps = length/slot + 1 //index
//      
//    	var pairsRDD = sparkSession.sparkContext.parallelize(pairs.toSeq).filter(p => p != null)
//    	val srdd = new SpatialRDD[Pair]
//      
//    	for(i <- 0 to steps){
//    	  
//    	  srdd.setRawSpatialRDD(pairsRDD.map(pr => {
//    	    new Pair(pr.getCoordinateBySlot(i*slot, slot), pr.getPrecisionModel, pr.getSRID)
//    	  }))
//    	  
//        srdd.analyze()
//        srdd.spatialPartitioning(GridType.QUADTREE, 10);
//        srdd.spatialPartitionedRDD.rdd.map(row => {
//      	  val lsRoute = row.getRoute
//      	  val time = row.getTime
//      	  val distance = row.getDistance
//      	  val routeInSec = new Interpolate().routeInterpolateBySec(lsRoute, time, distance, 1);
//      	  routeInSec
//      	})
//      	//write to file
//    	}

    	//seconds
//    	val duration = (System.nanoTime - t1) / 1e9d
//    	println(duration)
    }
  }
  
//  describe("Trajectories"){
//    it("generation test"){
//      val f = new FileOps()
//      f.createDirectory(resourceFolder + "/vistrips/routes")
//    }
//  }
  
//  describe("CountyPop"){
//    it("polygon join rectangle"){
//      CountyPop.run(sparkSession, 10)
//    }
//  }
  
//  describe("VisTrips Graph") {
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
    
//    it("Map fatest route test"){
//      //33.410065, -111.920412 2262996384
//      //33.406198, -111.939376 5662664860
//      var graph = new OsmGraph(sparkSession, hdfs)
//      val from = graph.findNearestByCoor(33.410065, -111.920412)
//      val to = graph.findNearestByCoor(33.406198, -111.939376)
//      println(from)
//      println(to)
//
//      val route = graph.fatestRouteRequest(33.410065, -111.920412, 33.406198, -111.939376)
//      route.legs.foreach(println)
      
//      var graph = OsmConverter.convertToNetwork(sparkSession, hdfs)
//      val routeRDD = ShortestPathFactory.runDijkstra(graph, 2262996384L, 5662664860L)
//      println(routeRDD.count())
//      routeRDD.filter(r => (r.legs.size > 0 && r.legs.last.getUserData.asInstanceOf[Long] == 5662664860L))
//              .foreach(println) 

      
//    }
    
//    it("spark graphx build in shortest path"){
//      var graph = new OsmGraph(sparkSession, hdfs)
//      val from = graph.findNearestByCoor(33.410065, -111.920412)
//      val to = graph.findNearestByCoor(33.406198, -111.939376)
//      println(from)
//      println(to)
//      val spResult = ShortestPaths.run(graph.graph, Seq(from.getUserData.asInstanceOf[Long], to.getUserData.asInstanceOf[Long]))
//        //Map(id: long -> landmarks count)
//      spResult.vertices.map(_._2).collect.foreach(println)
//    }
  
//  }

}
