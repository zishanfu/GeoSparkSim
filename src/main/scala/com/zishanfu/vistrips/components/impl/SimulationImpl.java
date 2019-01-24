package com.zishanfu.vistrips.components.impl;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;

import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.TrafficModelPanel;
import com.zishanfu.vistrips.sim.World;
import com.zishanfu.vistrips.sim.model.IDMVehicle;


public class SimulationImpl {
	private final Logger LOG = Logger.getLogger(SimulationImpl.class);
	private JavaRDD<IDMVehicle> vehicles;
	private OsmGraph graph;
	
	public SimulationImpl(JavaRDD<IDMVehicle> vehicles, OsmGraph graph) {
		this.vehicles = vehicles;
		this.graph = graph;
	}
	
	public void apply(double simTime) {
		World world = new World(graph, vehicles.rdd());
		
		//set simulation time
		try {
			//1min
			new TrafficModelPanel(world).run(simTime);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	
		
	}

}
