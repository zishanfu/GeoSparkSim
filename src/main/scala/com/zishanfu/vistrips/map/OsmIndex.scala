package com.zishanfu.vistrips.map

import org.datasyslab.geospark.spatialRDD.PointRDD
import org.datasyslab.geospark.enums.IndexType
import org.datasyslab.geospark.enums.FileDataSplitter
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate
import org.datasyslab.geospark.spatialOperator.KNNQuery
import org.apache.spark.sql.SparkSession
import org.datasyslab.geospark.spatialRDD.PolygonRDD
import org.apache.spark.api.java.JavaRDD.fromRDD
import org.apache.spark.api.java.JavaSparkContext.fromSparkContext



object OsmIndex {
  
  def nodeIndex(sparkSession : SparkSession) : PointRDD = {
    val df1 = sparkSession.read.parquet("/home/zishanfu/Downloads/node.parquet")
    val df2 = sparkSession.read.parquet("/home/zishanfu/Downloads/way.parquet")
    df1.printSchema
    
    val gf = new GeometryFactory()

    val rdd1 = df1.select("id", "latitude", "longitude").rdd.map(row => { 
      val point = gf.createPoint(new Coordinate(row(1).toString().toDouble, row(2).toString().toDouble))
      point.setUserData(row(0))
      point
     })
       
    rdd1.take(10).foreach(println)
    var objectRDD = new PointRDD(rdd1)
    objectRDD.buildIndex(IndexType.RTREE, false)
    val queryPoint = gf.createPoint(new Coordinate(33.3747897, -111.9080581))
    println("knn")
    var result = KNNQuery.SpatialKnnQuery(objectRDD, queryPoint, 5, true)
    
    for(r <- 0 to 4){
      println(result.get(r))
    }
    
    objectRDD
  }
  
  def CountyPolygonIndex(sparkSession : SparkSession) : Unit = {
    var df1 = sparkSession.read.json("/home/zishanfu/Downloads/datasets/population/popByCounty/gz_2010_us_050_00_500k.json")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("geometry.coordinates", "properties.COUNTY").filter("coordinates is not null and COUNTY is not null")
    val gf = new GeometryFactory()
    val PolygonRDDInputLocation = "/home/zishanfu/Downloads/datasets/population/popByCounty/gz_2010_us_050_00_500k.json"
    val PolygonRDDSplitter = FileDataSplitter.GEOJSON
    val PolygonRDDNumPartitions = 5
    val PolygonRDDStartOffset = 0
    val PolygonRDDEndOffset = 8
    var rdd = new PolygonRDD(sparkSession.sparkContext, PolygonRDDInputLocation, PolygonRDDStartOffset, PolygonRDDEndOffset, PolygonRDDSplitter, true)
    
  }
  
  def PopIndex(sparkSession : SparkSession) : Unit = {
    var df1 = sparkSession.read.format("csv").option("header", "true").load("/home/zishanfu/Downloads/datasets/population/popByCounty/co-est2017-alldata.csv")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("COUNTY", "CENSUS2010POP")
    
  }
  
  

}