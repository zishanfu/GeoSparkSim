package com.zishanfu.vistrips.network

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Coordinate

case class Link (id: Long, tail: Point, head: Point, distance: Double, 
    speed: Int, driveDirection: Int, lanes: Int) extends Serializable{
  
    def getTravelTime(): Double = distance / (speed) //mph
    
    def getId(): Long = this.id
    def getHead(): Point = this.head
    def getTail(): Point = this.tail    
    def getDistance(): Double = this.distance //mile
    def getSpeed(): Integer = this.speed
    def getDrivingDirection(): Integer = this.driveDirection
    def getLanes(): Integer = this.lanes
    
}