package com.zishanfu.vistrips.sim.model;

import java.util.*;

public class Intersection {
	List<TrafficLight> lights;
	Map<Segment, Integer> lightMap = new HashMap<>();
	Queue<Segment> uncontrollQueue = new LinkedList<>();
}
