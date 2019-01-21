package com.zishanfu.vistrips.sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.EdgeRDD;
import org.apache.spark.graphx.Graph;
import org.apache.spark.graphx.VertexRDD;
import org.apache.spark.rdd.RDD;

import com.zishanfu.vistrips.model.Vehicle;
import com.zishanfu.vistrips.model.Link;
import com.zishanfu.vistrips.sim.model.Point;
import com.zishanfu.vistrips.sim.model.Segment;
import com.zishanfu.vistrips.sim.ui.MapWindow;


public class TrafficModelPanel{
	private Graph<com.vividsolutions.jts.geom.Point, Link> graph;
	private JavaRDD<Vehicle> vehicles;
	private private RDD<com.vividsolutions.jts.geom.Point> uncontrollIntersect;
	private RDD<com.vividsolutions.jts.geom.Point> lightIntersect;
	
	public TrafficModelPanel(Graph<com.vividsolutions.jts.geom.Point, Link> graph, 
							JavaRDD<Vehicle> vehicles,
							RDD<com.vividsolutions.jts.geom.Point> uncontrollIntersect,
							RDD<com.vividsolutions.jts.geom.Point> lightIntersect) {
		this.graph = graph;
		this.vehicles = vehicles;
		this.uncontrollIntersect = uncontrollIntersect;
		this.lightIntersect = lightIntersect;
	}
	
	public void run() {
        EdgeRDD<Link> edges = graph.edges();
        
        MapWindow window = new MapWindow();
        window.setVisible(true);
        List<Link> links = edges.toJavaRDD().map(edge ->{
			return edge.attr;
		}).collect();

		for(int i = 0; i<links.size(); i++) {
			Link l = links.get(i);
			Point head = new Point(l.getHead().getCoordinate().y, l.getHead().getCoordinate().x);
			Point tail = new Point(l.getTail().getCoordinate().y, l.getTail().getCoordinate().x);
			window.addSegment(new Segment(head, tail, Color.GRAY, l.getLanes()));
		}
    }
	
	private java.awt.Point[] pointsMapping(VertexRDD<Point> vertexes){
		java.awt.Point[] points = {
                new java.awt.Point(-100, -100),
                new java.awt.Point(-100, 100),
                new java.awt.Point(100, -100),
                new java.awt.Point(100, 100)
        };
		return points;
	}
}
