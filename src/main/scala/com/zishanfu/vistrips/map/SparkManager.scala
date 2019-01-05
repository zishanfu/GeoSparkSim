package com.zishanfu.vistrips.map

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import com.zishanfu.vistrips.map.OsmIndex

object SparkManager {
  def main(args: Array[String]) : Unit = {
    val conf = new SparkConf().setAppName("OSMSpark").setMaster("local")
    val sparkSession = SparkSession.builder().config(conf).getOrCreate()
//    OsmIndex.nodeIndex(sparkSession)
//    OsmIndex.CountyPolygonIndex(sparkSession)
//    OsmIndex.PopIndex(sparkSession)
    OsmConverter.convertToNetwork(sparkSession)
  }
}