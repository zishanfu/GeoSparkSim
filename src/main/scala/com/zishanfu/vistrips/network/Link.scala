package com.zishanfu.vistrips.network

import com.vividsolutions.jts.geom.Coordinate

case class Link (id: Long, head: Long, tail: Long, distance: Double, 
    speed: Integer, driveDirection: Integer, lanes: Integer, points: Array[Coordinate]) extends Serializable{
  
    def getTravelTime(): Double = distance / (speed) //mph
    
    def getId(): Long = this.id
    def getHead(): Long = this.head
    def getTail(): Long = this.tail    
    def getDistance(): Double = this.distance //mile
    def getSpeed(): Integer = this.speed
    def getDrivingDirection(): Integer = this.driveDirection
    def getLanes(): Integer = this.lanes
    def getPoints() : Array[Coordinate] = this.points
    
}