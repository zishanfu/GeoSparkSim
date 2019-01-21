package com.zishanfu.vistrips.osm

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import com.vividsolutions.jts.geom.Coordinate
import scala.collection.mutable.WrappedArray
import org.datasyslab.geospark.spatialRDD.PolygonRDD
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Polygon
import org.datasyslab.geospark.enums.GridType
import org.datasyslab.geospark.spatialOperator.JoinQuery
import com.vividsolutions.jts.geom.Point
import org.apache.spark.api.java.JavaRDD.fromRDD
import scala.collection.Seq


object CountyPop {
  val gf = new GeometryFactory()
  
  def run(sparkSession : SparkSession, num : Int) : Unit = {
    val resourceFolder = System.getProperty("user.dir") + "/src/test/resources"
    // [33.41870367460316, -111.949276025639]
    // [33.382278218403826, -111.88402742589909]
    
    //[36.44196,-114.44681]
    //[31.72199,-108.34940]
//    val p1 = gf.createPoint(new Coordinate(-111.949276025639, 33.41870367460316))
//    val p2 = gf.createPoint(new Coordinate(-111.88402742589909, 33.382278218403826))
    
    val p1 = gf.createPoint(new Coordinate(-114.44681, 36.44196))
    val p2 = gf.createPoint(new Coordinate(-108.34940, 31.72199))
    //CountyPolygonIndex(sparkSession, resourceFolder, p1, p2)
    PopIndex(sparkSession, resourceFolder)
  }
  
  def CountyPolygonIndex(sparkSession : SparkSession, path : String, p1 : Point, p2 : Point) : Unit = {
    val PolygonEncoder = Encoders.kryo[Polygon]
    val CoordinateEncoder = Encoders.kryo[Coordinate]
    
    var df1 = sparkSession.read.json(path + "/popByCounty/us-counties.json")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("geometry.coordinates", "properties.name").filter("coordinates is not null and name is not null")
    var df3 = df2.select(col("name"), explode(col("coordinates")).as("coordinates"))
    
    var df4 = df3.map(row => {
      val countyId = row.getAs[String]("name")
      var arr = row.getAs[WrappedArray[WrappedArray[String]]]("coordinates")
      var coordinates : Array[Coordinate] = Array()
      if(arr.length == 1){
        val malformattedArr = arr.apply(0)
        for(i <- 0 until malformattedArr.length){
          val lon = malformattedArr(i)(0).toDouble
          val lat = malformattedArr(i)(1).toDouble
          coordinates = coordinates :+ new Coordinate(lon, lat)
        }
      }else{
        for(i <- 0 until arr.length){
          val lon = arr(i)(0).toDouble
          val lat = arr(i)(1).toDouble
          coordinates = coordinates :+ new Coordinate(lon, lat)
        }
      }
      if(coordinates.head != coordinates.last){
        null
      }else{
        val ring = gf.createLinearRing(coordinates.array)
        val polygon = gf.createPolygon(ring, null)
        polygon.setUserData(countyId)
        polygon
      }
    })(PolygonEncoder)

    var prdd = new PolygonRDD(df4.rdd)
    val considerBoundaryIntersection = true 
    val usingIndex = false
    
    val coordinates = Array( p1.getCoordinate, new Coordinate(p1.getX, p2.getY), p2.getCoordinate, new Coordinate(p2.getX, p1.getY), p1.getCoordinate)
    val rectangle = gf.createPolygon(gf.createLinearRing(coordinates), null)
    val rrdd = sparkSession.sparkContext.parallelize(Seq(rectangle))
    val queryWindowRDD = new PolygonRDD(rrdd)
    
    prdd.analyze()

    prdd.spatialPartitioning(GridType.KDBTREE)
    queryWindowRDD.spatialPartitioning(prdd.getPartitioner)

    val result = JoinQuery.SpatialJoinQuery(prdd, queryWindowRDD, usingIndex, considerBoundaryIntersection)
    result.rdd.foreach(println)
  }
  
  def PopIndex(sparkSession : SparkSession, path : String) : Unit = {
    val county = "Maricopa"
    var df1 = sparkSession.read.format("csv").option("header", "true").load(path + "/popByCounty/popByCounty.csv")
    df1.createOrReplaceTempView("df1")
    var df2 = df1.select("CTYNAME", "CENSUS2010POP")
    df2.filter(col("CTYNAME").contains(county))show()
  }
  

}