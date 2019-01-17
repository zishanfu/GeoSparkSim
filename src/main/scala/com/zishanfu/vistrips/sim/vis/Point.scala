package com.zishanfu.vistrips.sim.vis

class Point(val x: Double, val y: Double) {

  def --(other: Point): Double =
    math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))

  def ==(other: Point): Boolean = other match {
    case other: Point => (x == other.x) && (y == other.y)
    case null => false
  }


  override def toString: String = "(" + x + ", " + y + ")"

  override def hashCode(): Int = 41 * (41 + x.toInt) + y.toInt

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case other: Point => this == other
      case _ => false
    }
  }
}

object Point {
  def apply(x: Double, y: Double) = new Point(x, y)
}