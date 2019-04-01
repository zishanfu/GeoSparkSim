package com.zishanfu.geosparksim.model

import java.io.Serializable
import com.vividsolutions.jts.geom.Coordinate

case class Lane (id: Int, headLine: Coordinate, tailLine: Coordinate, head: Coordinate, tail: Coordinate) extends Serializable
