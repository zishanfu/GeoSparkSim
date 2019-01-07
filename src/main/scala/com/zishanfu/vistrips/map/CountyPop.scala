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



object CountyPop {
  
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