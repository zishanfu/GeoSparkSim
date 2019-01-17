package com.zishanfu.vistrips.sim.network

import com.zishanfu.vistrips.sim.traffic.TrafficResponse
import com.zishanfu.vistrips.sim.traffic.TrafficFlow

sealed trait Color extends Serializable with Product

object Color {
  case object RED extends Color
  case object YELLOW extends Color
  case object GREEN extends Color
  case object NONE extends Color
}

trait TrafficLight extends TrafficResponse {

  val intersection: Intersection

  val opposite: TrafficLight

  def apply(direction: Direction): TrafficFlow

  var durations: Map[Color, Double]

  var color: Color

  def extendColor(delta: Double)

  def time: Double

}

object TrafficLight{
  
}