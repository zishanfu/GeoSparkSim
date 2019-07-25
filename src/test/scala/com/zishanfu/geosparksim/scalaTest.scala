package com.zishanfu.geosparksim

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.{SparkConf, SparkContext}
import org.datasyslab.geospark.monitoring.GeoSparkListener
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.scalatest.{BeforeAndAfterAll, FunSpec}

class scalaTest extends FunSpec with BeforeAndAfterAll {
  implicit lazy val sc = {
    val conf = new SparkConf().setAppName("scalaTest").setMaster("local[2]")
    conf.set("spark.serializer", classOf[KryoSerializer].getName)
    conf.set("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)

    val sc = new SparkContext(conf)
    sc.addSparkListener(new GeoSparkListener)
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)
    sc
  }

  val resources: String = System.getProperty("user.dir") + "/src/test/resources"
  val f = new File(resources + "/scala-test")

  def deleteDirectory(): Unit = {
    if (f.isDirectory) {
      FileUtils.cleanDirectory(f)
      FileUtils.forceDelete(f)
    }
  }

  override def afterAll(): Unit = {
    sc.stop
  }

  describe("GeoSparkSim in Scala") {

  }

}
