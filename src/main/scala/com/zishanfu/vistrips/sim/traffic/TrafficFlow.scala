package com.zishanfu.vistrips.sim.traffic

import com.zishanfu.vistrips.sim.vis.Point
import com.zishanfu.vistrips.sim.vehicle.Vehicle
import com.zishanfu.vistrips.sim.network.TrafficLight
import com.zishanfu.vistrips.sim.network.Intersection
import scala.util.Random


trait TrafficFlow extends TrafficResponse{
  val start: Point

  val end: Point

  val lanes: Int

  val length: Double = start -- end
  
  def neighbour: TrafficFlow

  def vehicles: Seq[Vehicle]
  
  def intersections: Seq[Intersection]
  
  private[traffic] def +=(v: Vehicle): Unit

  private[traffic] def -=(v: Vehicle): Unit

  private val random = new Random(System.nanoTime())
  
  private[traffic] def randomLane = random.nextInt(lanes) + 1

  protected def ++=(intersection: Intersection): Unit
}
