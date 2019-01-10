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
import org.apache.spark.graphx.VertexRDD
import com.vividsolutions.jts.geom.Point
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.apache.spark.serializer.KryoSerializer



object CountyPop {
 
  def run(sparkSession : SparkSession, num : Int) : Unit = {
    val resourceFolder = System.getProperty("user.dir") + "/src/test/resources"
    CountyPolygonIndex(sparkSession, resourceFolder)
    PopIndex(sparkSession, resourceFolder)
  }
  
  def CountyPolygonIndex(sparkSession : SparkSession, path : String) : Unit = {
    var df1 = sparkSession.read.json(path + "/popByCounty/polygon.json")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("geometry.coordinates", "properties.COUNTY").filter("coordinates is not null and COUNTY is not null")
    df2.take(10).foreach(println)
    val gf = new GeometryFactory()
    val PolygonRDDInputLocation = path + "/popByCounty/polygon.json"
    val PolygonRDDSplitter = FileDataSplitter.GEOJSON
    val PolygonRDDNumPartitions = 5
    val PolygonRDDStartOffset = 0
    val PolygonRDDEndOffset = 8
    var rdd = new PolygonRDD(sparkSession.sparkContext, PolygonRDDInputLocation, PolygonRDDStartOffset, PolygonRDDEndOffset, PolygonRDDSplitter, true)
    
  }
  
  def PopIndex(sparkSession : SparkSession, path : String) : Unit = {
    var df1 = sparkSession.read.format("csv").option("header", "true").load(path + "/popByCounty/popByCounty.csv")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("COUNTY", "CENSUS2010POP")
    
  }
  
  def StatsFormat() : Unit ={
    
  }
  
  

}