package com.zishanfu.vistrips.sim.model;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.EdgeRDD;
import org.apache.spark.graphx.VertexRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.vistrips.model.Link;
import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.TrafficModelPanel;

import scala.collection.immutable.Set;


public class World {
	//graph: OsmGraph, vehicle: RDD[IDMVehicle]
	private OsmGraph graph;
	private JavaRDD<IDMVehicle> vehicle;
	private JavaRDD<IDMVehicle> roadVehicles;
	private final Logger LOG = Logger.getLogger(TrafficModelPanel.class);
	
	public World(OsmGraph graph, JavaRDD<IDMVehicle> vehicle) {
		this.graph = graph;
		this.vehicle = vehicle;
		this.roadVehicles = roadDigesting();
	}
	
	

	public OsmGraph getGraph() {
		return graph;
	}


	public JavaRDD<IDMVehicle> getRoadVehicles() {
		return roadVehicles;
	}

	private JavaRDD<IDMVehicle> roadDigesting(){
		Set<Coordinate> signals = graph.getIntersectsSet();
		Set<Coordinate> intersects = graph.getSignalsSet();
		EdgeRDD<Link> edges = graph.graph().edges();
		VertexRDD<com.vividsolutions.jts.geom.Point> vertex = graph.graph().vertices();
		LOG.warn("edges and vertices: " + edges.count() +", " + vertex.count());
		
		JavaRDD<IDMVehicle> landedVehicles = vehicle.map(veh -> {
			Coordinate[] coordinates = veh.getCoordinates();
			VirtualGPS gps = veh.getGps();
			for(Coordinate coor: coordinates) {
				if(signals.contains(coor)) {
					gps.addLights(coor);
				}else if(intersects.contains(coor)){
					gps.addIntersects(coor);
				}
			}
			veh.setGps(gps);
			return veh;
		});
		return landedVehicles;
	}
}
