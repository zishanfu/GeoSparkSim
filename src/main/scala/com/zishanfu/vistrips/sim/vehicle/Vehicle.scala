package com.zishanfu.vistrips.sim.vehicle

import com.zishanfu.vistrips.sim.traffic.TrafficFlow
import com.zishanfu.vistrips.sim.traffic.TrafficResponse
import com.zishanfu.vistrips.sim.network.Direction
import com.zishanfu.vistrips.sim.vis.Point


trait Vehicle extends TrafficResponse {

  def trafficFlow: TrafficFlow

  def distance: Double

  def lane: Int

  def speed: Double

  def acceleration: Double

  val length: Double

  def direction: Direction

  val timeToTurnRight: Double = 0.5

  val timeToTurnLeft: Double = 2

  val timeToTurnBack: Double = 2

}