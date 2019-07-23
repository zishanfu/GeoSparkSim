package com.zishanfu.geosparksim

import com.zishanfu.geosparksim.Tools.FileOps
import com.zishanfu.geosparksim.osm.{OsmConverter, RoadNetworkWriter}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
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

  override def afterAll(): Unit = {
    sc.stop
  }

  describe("GeoSparkSim in Scala") {

    val resourceFolder = System.getProperty("user.dir") + "/src/test/resources/"

//    it("HDFS operations") {
//      val outputPath = "geosparksim"
//      val hdfs = "hdfs://localhost:9000"
//      val hdfsUtil = new HDFSUtil(hdfs)
//      hdfsUtil.deleteDir("/" + outputPath)
//      hdfsUtil.mkdir("/" + outputPath)
//      assert(hdfsUtil.checkHDFS("/" + outputPath))
//    }

    it("OSM Parquet to geosparksim files"){
      val ss = SparkSession.builder.config(sc.getConf).getOrCreate
      val resources = System.getProperty("user.dir") + "/src/test/resources"
      new FileOps().createDirectory(resources + "/java-test")
      val path = resources + "/samples"
      val roadNetwork = OsmConverter.convertToRoadNetwork(ss, path)
      val networkWriter = new RoadNetworkWriter(ss, roadNetwork, resources + "/java-test")
      networkWriter.writeEdgeJson()
      networkWriter.writeSignalJson()
      networkWriter.writeIntersectJson()
    }

  }

}
