package com.zishanfu.vistrips.map

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import com.zishanfu.vistrips.map.OsmIndex
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.apache.spark.serializer.KryoSerializer

object SparkManager {
  def main(args: Array[String]) : Unit = {
    run("default")
  }
  
  def run(path: String) : Unit = {
    val conf = new SparkConf().setAppName("OSMSpark").setMaster("local")
    val sparkSession = SparkSession.builder().config(conf)
                      .config("spark.serializer", classOf[KryoSerializer].getName)
                      .config("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)
                      .getOrCreate()
//    OsmIndex.nodeIndex(sparkSession)
//    OsmIndex.CountyPolygonIndex(sparkSession)
//    OsmIndex.PopIndex(sparkSession)
    OsmConverter.convertToNetwork(sparkSession, path)
  }
}