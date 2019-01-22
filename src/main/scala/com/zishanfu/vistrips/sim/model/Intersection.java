package com.zishanfu.vistrips.sim.model;

import java.util.*;

public class Intersection {
	//1 -> light intersection
	//0 -> uncontrol intersection
	private static final int TYPE = 0;
	List<TrafficLight> lights;
	Map<Segment, Integer> lightMap = new HashMap<>();
	Queue<Segment> uncontrollQueue = new LinkedList<>();
}
