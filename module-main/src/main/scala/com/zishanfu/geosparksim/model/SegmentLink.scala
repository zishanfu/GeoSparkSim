package com.zishanfu.geosparksim.model

import java.io.Serializable

case class SegmentLink(id: Long, head: SegmentNode, tail: SegmentNode, distance: Double,
                       speed: Int, driveDirection: Int, lanes: Int) extends Serializable{

    def getTravelTime: Double = distance / speed //mph

}