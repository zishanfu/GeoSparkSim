package com.zishanfu.vistrips.sim;


import java.awt.Color;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.EdgeRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.vistrips.model.Link;
import com.zishanfu.vistrips.model.Vehicle;
import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.model.GeoPoint;
import com.zishanfu.vistrips.sim.model.Point;
import com.zishanfu.vistrips.sim.model.Segment;
import com.zishanfu.vistrips.sim.ui.MapWindow;


public class TrafficModelPanel{
	private World world;
	private JavaRDD<Vehicle> lastVehicles;
	
	public TrafficModelPanel(World world) {
		this.world = world;
	}
	
	public void run(int seconds) {
		OsmGraph osmGraph = world.getGraph();
        EdgeRDD<Link> edges = osmGraph.graph().edges();
        lastVehicles = world.getVehicles();
        
        MapWindow window = new MapWindow();
        window.setVisible(true);
        
        //need to join link and vehicle
        //get linestring with link information
        
        //paint streets
        List<Link> links = edges.toJavaRDD().map(edge ->{
			return edge.attr;
		}).collect();

		for(int i = 0; i<links.size(); i++) {
			Link l = links.get(i);
			Point head = new Point(l.getHead().getCoordinate().y, l.getHead().getCoordinate().x);
			Point tail = new Point(l.getTail().getCoordinate().y, l.getTail().getCoordinate().x);
			window.addSegment(new Segment(head, tail, Color.GRAY, l.getLanes()));
		}
		
		//paint signals
		window.addSignals(osmGraph.getSignals());
		
		int iterations = (int) (seconds / 0.2);
		
		
		
		vehicleIterator(lastVehicles);

		//repartition at certain time
//		for(int i = 0; i<iterations; i++) {
//			
//			JavaRDD<Vehicle> newVRDD = vehicleIterator(lastVehicles);
//			
//			//sample or run in GPU
//			List<Vehicle> vehicles = newVRDD.collect();
//			for(Vehicle v: vehicles) {
//				Coordinate coor = v.getCurCoordinate();
//				window.addPOI(new GeoPoint(coor.y, coor.x));
//			}
//			
//			lastVehicles = newVRDD;
//		}
    }
	
	//0.2 seconds
	private JavaRDD<Vehicle> vehicleIterator(JavaRDD<Vehicle> vehicles) {
		//intersection map update lights
		
		//vehicle update
		//check if the point is in intersection
		//if traffic signal intersect
		//red -> keep same location, green -> move forward
		//if uncontrol intersect 
		//queue the first arrive car, others keep same
		//if no in intersect 
		//check buffer car 
		//IDM direction
		
		//create circle RDD from current vehicles with buffer
		//join circle rdd and vehicle rdd
		//return vehicle rdd
		
		
//		vehicles = vehicles.map(veh -> {
//			veh.setCurCoordinate(veh.getNext());
//			return veh;
//		});
		
		return vehicles;
	}
	
}
