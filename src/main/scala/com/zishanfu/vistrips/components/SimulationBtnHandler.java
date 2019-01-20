package com.zishanfu.vistrips.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.Graph;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.JXMapViewer;

import com.vividsolutions.jts.geom.Point;
import com.zishanfu.vistrips.model.Vehicle;
import com.zishanfu.vistrips.network.Link;
import com.zishanfu.vistrips.sim.TrafficModelPanel;

public class SimulationBtnHandler implements ActionListener{

	private JXMapViewer mapViewer;
	private JavaRDD<Vehicle> vehicles;
	private SparkSession sparkSession;
	private double delayInSec;
	private int routeLength;
	private Graph<Point, Link> graph;
	
	public SimulationBtnHandler(JXMapViewer mapViewer, SparkSession sparkSession) {
		this.mapViewer = mapViewer;
		this.sparkSession = sparkSession;
	}
	
	
	public void setDelayInSec(double delayInSec) {
		this.delayInSec = delayInSec;
	}

	public void setVehicles(JavaRDD<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}


	public void setRouteLength(int routeLength) {
		this.routeLength = routeLength;
	}

	public void setGraph(Graph<Point, Link> graph) {
		this.graph = graph;
	}


	public void actionPerformed(ActionEvent e) {
		if(vehicles == null || vehicles.count() == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"Please wait to generate trips!");
		
		}else {
//			int delay = (int)(delayInSec*1000);
//			final Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>();
//			
//			for(Pair p: pairs) {
//				if(p == null) {
//					continue;
//				}
//				waypoints.add(new MyWaypoint(Color.black, p.getRoute()));
//			}
//			
//			final WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
//			waypointPainter.setWaypoints(waypoints);
//			mapViewer.setOverlayPainter(waypointPainter);
//			waypointPainter.setRenderer(new PointRender());
//
//			Timer timer = new Timer(delay, new ActionListener() {
//	            public void actionPerformed(ActionEvent e) {
//	                for (MyWaypoint waypoint: waypoints) {
//	                	waypoint.update();
//	                }
//	                mapViewer.repaint();
//	            }
//	        });
//
//	        timer.start();
			new TrafficModelPanel(graph, mapViewer).run();
		}
		
	}

}
