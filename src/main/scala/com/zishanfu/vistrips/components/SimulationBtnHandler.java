package com.zishanfu.vistrips.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

import com.zishanfu.vistrips.components.impl.SimulationImpl;
import com.zishanfu.vistrips.model.MyWaypoint;
import com.zishanfu.vistrips.model.Pair;

public class SimulationBtnHandler implements ActionListener{

	private JXMapViewer mapViewer;
	private Pair[] pairs;
	private SparkSession sparkSession;
	private double delayInSec;
	private int routeLength;
	
	public SimulationBtnHandler(JXMapViewer mapViewer, SparkSession sparkSession) {
		this.mapViewer = mapViewer;
		this.sparkSession = sparkSession;
	}
	
	
	public void setDelayInSec(double delayInSec) {
		this.delayInSec = delayInSec;
	}


	public void setPairs(Pair[] pairs) {
		this.pairs = pairs;
	}

	public void setRouteLength(int routeLength) {
		this.routeLength = routeLength;
	}


	public void actionPerformed(ActionEvent e) {
		if(pairs == null || pairs.length == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"Please wait to generate trips!");
		
		}else {
			int delay = (int)(delayInSec*1000);
			final Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>();
			
			for(Pair p: pairs) {
				if(p == null) {
					continue;
				}
				waypoints.add(new MyWaypoint(Color.black, p.getRoute()));
			}
			
			final WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
			waypointPainter.setWaypoints(waypoints);
			mapViewer.setOverlayPainter(waypointPainter);
			waypointPainter.setRenderer(new PointRender());

			Timer timer = new Timer(delay, new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                for (MyWaypoint waypoint: waypoints) {
	                	waypoint.update();
	                }
	                mapViewer.repaint();
	            }
	        });

	        timer.start();
		}
		
	}

}
