package com.zishanfu.vistrips.map;

import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.PointList;

public interface Graph {
	long getTotalNodes();
	GeoPosition getCoorById(long id);
	GeoPosition getClosestNode(GeoPosition node);
	PointList routeRequest(double latFrom, double lonFrom, double latTo, double lonTo);
	double routeDistCompute(double[] src, double[] des);
}
