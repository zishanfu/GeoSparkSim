package com.zishanfu.vistrips.sim.events

import com.zishanfu.vistrips.sim.vehicle.Vehicle
import com.zishanfu.vistrips.sim.traffic.TrafficFlow
import com.zishanfu.vistrips.sim.network.Direction
import com.zishanfu.vistrips.sim.network.Intersection


trait VehicleEvent extends TrafficEvent

case class VehicleSpawned(vehicle: Vehicle) extends VehicleEvent

case class VehicleRemoved(vehicle: Vehicle) extends VehicleEvent

case class IntersectionPassed(vehicle: Vehicle, intersection: Intersection) extends VehicleEvent

case class TrafficFlowChanged(vehicle: Vehicle, oldFlow: TrafficFlow, direction: Direction) extends VehicleEvent

case class LaneChanged(vehicle: Vehicle, oldLane: Int) extends VehicleEvent

case class VehicleStopped(vehicle: Vehicle) extends VehicleEvent

case class VehicleMoved(vehicle: Vehicle) extends VehicleEvent