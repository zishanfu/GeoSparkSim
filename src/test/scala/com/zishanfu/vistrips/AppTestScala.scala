package com.zishanfu.vistrips

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.algorithm.Angle


class AppTestScala extends TestBaseScala {
  
//  describe("IDM"){
//    it("Simulation Test"){
//      val c1 = new Coordinate(33.4148172, -111.9262878);
//      val c11 = new Coordinate(33.41482,-111.92638);
//      val c2 = new Coordinate(33.4148230, -111.9264849);
//      val c3 = new Coordinate(33.4148076, -111.9273659);
//      val route1 : Array[Coordinate] = Array(c1, c2, c3);
//      val route2 : Array[Coordinate] = Array(c2, c3);
//
//      val vehicle1 = new IDMVehicle(route1, 1, 10, 10)
//      val vehicle2 = new IDMVehicle(route2, 2, 10, 10)
//      import scala.collection.JavaConverters._
//      val javaSet = new java.util.HashSet[IDMVehicle]()
//      
//      print(c1.distance(c11)/0.0000021)
//      javaSet.add(vehicle2)
//      println(vehicle1.getReport.toString())
//      vehicle1.setAheadVehicles(javaSet)
//      vehicle1.moveNext()
//      println(vehicle1.getReport.toString())
//      vehicle1.moveNext()
//      println(vehicle1.getReport.toString())
//      
//    }
//  }
  
  
  //1:479km = euclidean: harvsine
	//Car 4.5m
	//safe distance 2m
	//11m
	//1/x = 479000/11
	//x = 0.000023
  //0.00000772 -> 3.7m lane width
  describe("Angle Test"){
    it("two coordinates"){
      //33.4148230, -111.9264849,tail
      //33.4148172, -111.9262878,head
      val p = new Coordinate(33.4148230, -111.9264849)
      val q = new Coordinate(33.4148172, -111.9262878)
      val length = 0.00005
      val headWidth = 0.00000772
      //0.000023
      //0.000002
      val backWidth = 0.00002317
      println(p.x + "," + p.y + ", tail")
      
      //10m
      val headAngle = Angle.angle(p, q) //p -> q
      val dx = Math.cos(headAngle)*length
      val dy = Math.sin(headAngle)*length
      val x = dx + p.x
      val y = dy + p.y
      val p1 = new Coordinate(x, y)
      println(x + "," + y + ", head")
      
      if(dx < dy){
        val p11 = new Coordinate(p1.x + headWidth, p1.y);
        val p12 = new Coordinate(p1.x - headWidth, p1.y);
        val p21 = new Coordinate(p.x + headWidth, p.y);
        val p22 = new Coordinate(p.x - headWidth, p.y);
        System.out.println(p11 + ", head1");
        System.out.println(p12 + ", head2");
        System.out.println(p21 + ", head3");
        System.out.println(p22 + ", head4");
      }else{
        val p11 = new Coordinate(p1.x, p1.y + headWidth);
        val p12 = new Coordinate(p1.x, p1.y - headWidth);
        val p21 = new Coordinate(p.x, p.y + headWidth);
        val p22 = new Coordinate(p.x, p.y - headWidth);
        System.out.println(p11 + ", head1");
        System.out.println(p12 + ", head2");
        System.out.println(p21 + ", head3");
        System.out.println(p22 + ", head4");
      }
    }
//      
//      
//      val backAngle = Angle.angle(q, p) //q -> p
//      val dx1 = Math.cos(backAngle)*length
//      val dy1 = Math.sin(backAngle)*length
//      val x1 = dx1 + p.x
//      val y1 = dy1 + p.y
//      val p2 = new Coordinate(x1, y1);
//      println(x1 + "," + y1 + ", back")
//      
//      if(dx1 > dy1){
//        val p11 = new Coordinate(p2.x + backWidth, p2.y);
//        val p12 = new Coordinate(p2.x - backWidth, p2.y);
//        val p21 = new Coordinate(p.x + backWidth, p.y);
//        val p22 = new Coordinate(p.x - backWidth, p.y);
//        System.out.println(p11 + ", back1");
//        System.out.println(p12 + ", back2");
//        System.out.println(p21 + ", back3");
//        System.out.println(p22 + ", back4");
//      }else{
//        val p11 = new Coordinate(p2.x, p2.y + backWidth);
//        val p12 = new Coordinate(p2.x, p2.y - backWidth);
//        val p21 = new Coordinate(p.x, p.y + backWidth);
//        val p22 = new Coordinate(p.x, p.y - backWidth);
//        System.out.println(p11 + ", back1");
//        System.out.println(p12 + ", back2");
//        System.out.println(p21 + ", back3");
//        System.out.println(p22 + ", back4");
//      }
//      
//      
//    }
//  }
  
//  describe("Intersection"){
//    it("Traffic Light Intersection"){
//      val vistrips = resourceFolder + "vistrips"
//      val path = vistrips
//      OsmConverter.convertToNetwork(sparkSession, path)
//    }
//  }
  
//  describe("Graphhopper Test"){
//    it("generate routes"){
//        //initial osm
//        val geo1 = new GeoPosition(33.41870367460316, -111.949276025639)
//        val geo2 = new GeoPosition(33.382278218403826, -111.88402742589909)
//        val maxLen = new Distance().euclidean(geo1, geo2) / 10
//        val newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen)
//		    val newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen)
//        val osmPath = new GenerationImpl(sparkSession).osmDownloader(newGeo1, newGeo2)
//        //graphhopper initail
//        val graph = new GraphInit(osmPath);
//        val src = Array(33.408892, -111.925377)
//        val dest = Array(33.406941, -111.935163)
//        graph.printInstruction(src, dest)
//      }
//    }
//  
//    describe("Generation Test"){
//      it("download osm by app setting in resources/config/app.config, generate source, destination and route of it"){
//        val simConsole = new JmapConsole(resourceFolder, sparkSession);
//        //generate vehicle rdd
//        //graphhopper serializable issue 
//    	  simConsole.runGeneration();
//      }
//    }
    
    
    
//  describe("Routes"){
//    it("generation test"){
//      // p1: [33.414964957503585, -111.94467544555664], p2: [33.39031619194356, -111.89120292663574]
//    	val geo1 = new GeoPosition(33.414964957503585, -111.94467544555664)
//    	val geo2 = new GeoPosition(33.39031619194356, -111.89120292663574)
//    	val selectedType = "DSO"
//    	val maxLen = new Distance().euclidean(geo1, geo2) / 10; 
//    	val newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen)
//		  val newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen)
//		  
//		  val osmloader = new OsmLoader(newGeo1, newGeo2)
//		  val path = osmloader.download()
    	
//      val graphhopper = new GraphInit(resourceFolder + "/vistrips/2019-01-17T11:01:34Z.osm")
//      val nums = 1000
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
//    }
//  }
  
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
      //33.410065, -111.920412 2262996384
      //33.406198, -111.939376 5662664860
      
//      val from = graph.findNearestByCoor(33.410065, -111.920412)
//      val to = graph.findNearestByCoor(33.406198, -111.939376)
//      println(from)
//      println(to)
//      case class requests(fromLat: Double, fromLon: Double, toLat : Double, toLon : Double)
//      val requestRDD = sparkSession.sparkContext.parallelize(Seq(requests(33.410065, -111.920412, 33.406198, -111.939376),
//                                                requests(33.410065, -111.920412, 33.406198, -111.939376),
//                                                requests(33.410065, -111.920412, 33.406198, -111.939376)))
//      requestRDD.foreach(req =>{
//        graph.fatestRouteRequest(req.fromLat, req.fromLon, req.toLat, req.toLon)
//      })
      //val route = graph.fatestRouteRequest(33.410065, -111.920412, 33.406198, -111.939376)
//      route.legs.foreach(println)
      
//      val requestRDD = sparkSession.sparkContext.parallelize(Seq((2262996384L, 5662664860L),
//                                                (2262996384L, 5662664860L),
//                                                (2262996384L, 5662664860L)))
//      requestRDD.foreach(req =>{
//        val route = ShortestPaths.run(graph, Seq(req._1, req._2))
//      })
      
      
//      var graph = OsmConverter.convertToNetwork(sparkSession, hdfs)
//      val routeRDD = BulkShortestPath.runDijkstra(graph, 2262996384L, 5662664860L)
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
//  
  }

}
