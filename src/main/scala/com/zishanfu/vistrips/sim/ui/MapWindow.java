package com.zishanfu.vistrips.sim.ui;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JFrame;

import com.zishanfu.vistrips.sim.model.GeoPoint;
import com.zishanfu.vistrips.sim.model.Segment;


@SuppressWarnings("serial")
public class MapWindow extends JFrame {
	private final MapPanel map;
	
	/**
	 * Creates a new window.
	 */
	public MapWindow() {
		super("GeoSparkSim");
		map = new MapPanel();
		setLayout(new BorderLayout());
		add(map, BorderLayout.CENTER);
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		
	}
	
	/**
	 * Deletes all the registered segments and POIs.
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * Adds a segment to the list of segments to display.
	 * @param segment the segment to add
	 */
	public void addSegment(Segment segment) {
		map.addSegment(segment);
	}
	
	/**
	 * Adds a whole collection of segments to the list of segments to display
	 * @param segments the collection of segments to add
	 */
	public void addSegments(Collection<Segment> segments) {
		map.addSegments(segments);
	}
	
	/**
	 * Adds a point of interest (POI) to the list of POIs to display. 
	 * @param poi the POI to add
	 */
	public void addPOI(GeoPoint poi) {
		map.addPOI(poi);
	}
}
