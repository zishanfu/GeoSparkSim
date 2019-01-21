package com.zishanfu.vistrips.model

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.LineString
import org.geotools.geometry.jts.JTSFactoryFinder

case class Route(destination: Long, distance : Double, time : Double, legs : List[Point]) extends Serializable{
  def getDestination(): Long = this.destination
  def getDistanceInMile(): Double = this.distance 
  def getDistanceInKilometer(): Double = this.distance*1.609344
  def getTimeInSecond(): Double = this.time*60*60
  def getTimeInHour(): Double = this.time
  def getLegsListPoint(): List[Point] = this.legs
  def getLegsLineString(): LineString = {
    val gf = JTSFactoryFinder.getGeometryFactory();
    gf.createLineString(legs.map(l => l.getCoordinate).toArray)
  }
}