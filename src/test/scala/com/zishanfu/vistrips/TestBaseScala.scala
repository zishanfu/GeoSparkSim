package com.zishanfu.vistrips

import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.scalatest.{BeforeAndAfterAll, FunSpec}

trait TestBaseScala extends FunSpec with BeforeAndAfterAll{
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("org.datasyslab.geospark").setLevel(Level.WARN)

  val warehouseLocation = System.getProperty("user.dir") + "/target/"
  var sparkSession = SparkSession.builder().config("spark.serializer", classOf[KryoSerializer].getName).
    config("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName).
    master("local[*]").appName("geosparksqlScalaTest")
    .config("spark.sql.warehouse.dir", warehouseLocation)
    .config("spark.local.dir", warehouseLocation + "/tmp/spark-temp").getOrCreate()

  val resourceFolder = System.getProperty("user.dir") + "/src/test/resources/"
  val csvData = resourceFolder+"arealm-small.csv"
  val hdfs = "hdfs://localhost:9000/vistrips/20190110_153248"
  val nodesPath = hdfs + "/node.parquet"
  val waysPath = hdfs + "/way.parquet"

  override def beforeAll(): Unit = {
  }

  override def afterAll(): Unit = {
  }
}
