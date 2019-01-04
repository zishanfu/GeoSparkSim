package com.zishanfu.vistrips.map.MapServiceImpl

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.datasyslab.geospark.spatialRDD.PointRDD
import org.datasyslab.geospark.spatialRDD.LineStringRDD
import org.datasyslab.geospark.enums.IndexType
import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.LineString
import org.datasyslab.geospark.spatialOperator.KNNQuery
import org.apache.spark.sql.SparkSession
import org.datasyslab.geospark.spatialRDD.PolygonRDD



object OsmIndex {
  
  def nodeIndex(sparkSession : SparkSession) : PointRDD = {
    val df1 = sparkSession.read.parquet("/home/zishanfu/Downloads/node.parquet")
    val df2 = sparkSession.read.parquet("/home/zishanfu/Downloads/way.parquet")
    df1.printSchema
    
    val gf = new GeometryFactory()

    val rdd1 = df1.select("id", "latitude", "longitude").rdd.map(row => gf.createPoint(new Coordinate(row(1).toString().toDouble, row(2).toString().toDouble)))
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
  
  def PolygonRDD(sparkSession : SparkSession) : Unit = {
    var df1 = sparkSession.read.json("/home/zishanfu/Downloads/datasets/population/popByCounty/gz_2010_us_050_00_500k.json")
    df1.createOrReplaceTempView("df1")
    df1.show()
  }
  
  

}