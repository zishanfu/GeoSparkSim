package com.zishanfu.vistrips.map

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import com.zishanfu.vistrips.network.Link
import scala.collection.mutable.WrappedArray

object OsmConverter {
  
  def convertToNetwork(sparkSession : SparkSession) : Unit = {
    val nodesDF = convertNodes(sparkSession)
    val linksDF = convertLinks(sparkSession, nodesDF)
  }
  
  private def convertNodes(sparkSession : SparkSession) : DataFrame = {
    val nodesDF = sparkSession.read.parquet("/home/zishanfu/Downloads/node.parquet")
    nodesDF.select("id", "latitude", "longitude")
  }
  
  private def convertLinks(sparkSession : SparkSession, nodesDF : DataFrame) : Unit = {
    val defaultSpeed = 40 //40mph
    val waysDF : Dataset[Row] = sparkSession.read.parquet("/home/zishanfu/Downloads/way.parquet")
    waysDF.printSchema()
    var df1 = waysDF.select("nodes").withColumn("nodes", waysDF("nodes.nodeId"))
    var intersectsDF = df1.select(explode(col("nodes")).as("nodeId")).groupBy("nodeId").count().filter(col("count") >= 2).select("nodeId")
    
    //var linkDF : Dataset[Link] = 
    
    waysDF.flatMap( (row: Row) => {
      var links : List[Link] = List.empty[Link]
      var tagsMap = Map.empty[String, String]
      var tags = row.getAs[WrappedArray[Row]](1)
      tagsMap = tags.map(r => new String(r.getAs[Array[Byte]]("key")) -> new String(r.getAs[Array[Byte]]("value"))).toMap
      var speed = defaultSpeed
      var maxSpeed = tagsMap.get("maxspeed")
      if(!maxSpeed.isEmpty) speed = maxSpeed.get.toInt
      
      val isOneWay = tagsMap.getOrElse("oneway", "no") == "yes"
      val lanes = tagsMap.get("lanes")
      
      var nodes = row.getAs[WrappedArray[Row]](2)
                    .map(r => (r.getInt(0), r.getAs[Long](1))).array
                    .sortBy(x => x._1)
      var linkIds = row.getAs[WrappedArray[Long]](0).toArray
      
      nodes.sliding(2).map(group => {
        println(group)
      })
      
      links
    })(Encoders.product[Link])
    
  }
  
  
}