package com.zishanfu.vistrips.simulation

import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import com.zishanfu.vistrips.JmapConsole
import org.apache.spark.serializer.KryoSerializer
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator

trait SimulationTestBase extends FunSpec with BeforeAndAfterAll{
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

  val resourceFolder = System.getProperty("user.dir") + "/src/test/resources"
  val simConsole = new JmapConsole(resourceFolder, sparkSession);
  simConsole.runGeneration();
  
  override def beforeAll(): Unit = {

    
  }

  override def afterAll(): Unit = {
    
  }
}
