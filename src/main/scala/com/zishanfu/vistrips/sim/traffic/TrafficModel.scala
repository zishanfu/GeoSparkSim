package com.zishanfu.vistrips.sim.traffic

import akka.actor._
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import com.zishanfu.vistrips.sim.network.TrafficLight
import com.zishanfu.vistrips.sim.vehicle.Vehicle
import com.zishanfu.vistrips.sim.network.Intersection
import com.zishanfu.vistrips.sim.vis.Point
import com.zishanfu.vistrips.sim.traffic.TrafficFlow
import com.zishanfu.vistrips.sim.network.Intersection



trait TrafficModel extends TrafficResponse{
  private val config = ConfigFactory.load()
    .withValue("akka.log-dead-letters", ConfigValueFactory.fromAnyRef("OFF"))
    
  private[traffic] val actorSystem: ActorSystem = ActorSystem("system", config)
  
  def run()
  
  def run(time: Double): Unit
  
  def asyncRun(time: Double): Unit

  def stop()

  def isRunning: Boolean

  def trafficFlows: Seq[TrafficFlow]

  def intersections: Seq[Intersection]

  def trafficLights: Seq[TrafficLight]
  
  def vehicles: Seq[Vehicle]

  def currentTime: Double

  def +=(flow: TrafficFlow): TrafficModel
  
}