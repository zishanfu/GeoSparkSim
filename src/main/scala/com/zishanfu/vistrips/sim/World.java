package com.zishanfu.vistrips.sim;

import java.awt.Graphics2D;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.Graph;
import org.apache.spark.rdd.RDD;

import com.vividsolutions.jts.geom.Point;
import com.zishanfu.vistrips.model.Link;
import com.zishanfu.vistrips.model.Vehicle;
import com.zishanfu.vistrips.osm.OsmGraph;


public class World {
	private OsmGraph graph;
	private JavaRDD<Vehicle> vehicles;
	
	public World(OsmGraph graph, JavaRDD<Vehicle> vehicles) {
		this.graph = graph;
		this.vehicles = vehicles;
	}

	public OsmGraph getGraph() {
		return graph;
	}

	public JavaRDD<Vehicle> getVehicles() {
		return vehicles;
	}
	
	
	
}
