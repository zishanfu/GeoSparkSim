package com.zishanfu.vistrips.map.MapServiceImpl

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf

object SparkManager {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("OSMSpark").setMaster("local")
    val sparkSession = SparkSession.builder().config(conf).getOrCreate()
    OsmIndex.PolygonRDD(sparkSession)
  }
}