package com.zishanfu.vistrips.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

import com.zishanfu.vistrips.model.MyWaypoint;
import com.zishanfu.vistrips.model.Pair;

public class SimulationBtnHandler implements ActionListener{
	
	private JXMapViewer mapViewer;
	private Pair[] pairs;
	
	public SimulationBtnHandler(JXMapViewer mapViewer) {
		this.mapViewer = mapViewer;
	}

	public void setPairs(Pair[] pairs) {
		this.pairs = pairs;
	}

	public void actionPerformed(ActionEvent e) {
		if(pairs == null || pairs.length == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"Please wait to generate trips!");
		
		}else {
			final Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>();
			for(Pair p: pairs) {
				if(p == null) {
					continue;
				}
				waypoints.add(new MyWaypoint(Color.black, p.getRoute().toGeoJson()));
			}
			
			final WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
			waypointPainter.setWaypoints(waypoints);
			mapViewer.setOverlayPainter(waypointPainter);
			waypointPainter.setRenderer(new PointRender());
		
			Timer timer = new Timer(1000, new ActionListener() {
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
