package com.zishanfu.vistrips.sim;

import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JFrame;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.EdgeRDD;
import org.apache.spark.graphx.Graph;
import org.apache.spark.graphx.VertexRDD;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.zishanfu.vistrips.network.Link;
import com.zishanfu.vistrips.sim.model.Lane;

public class TrafficModelPanel{
	private Graph<Point, Link> graph;
	private JXMapViewer mapViewer;
	
	public TrafficModelPanel(Graph<Point, Link> graph, JXMapViewer mapViewer) {
		this.graph = graph;
		this.mapViewer = mapViewer;
	}
	
	public void run() {
        JFrame frame = new JFrame("GeoSparkSim");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        EdgeRDD<Link> edges = graph.edges();
        VertexRDD<Point> vertexes = graph.vertices();
        
        ZoomAndPanCanvas chart = new ZoomAndPanCanvas(linesMapping(edges));

        frame.add(chart, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        chart.createBufferStrategy(2);
    }
	
	private java.awt.Point Coordinate2Point(Coordinate coor){
		GeoPosition geo = new GeoPosition(coor.y, coor.x);
		Point2D p = mapViewer.getTileFactory().geoToPixel(geo, mapViewer.getZoom());
		return new java.awt.Point((int)p.getX(), (int)p.getY());
	}
	
	private Lane[] linesMapping(EdgeRDD<Link> edges){
		List<Link> links = edges.toJavaRDD().map(edge ->{
			return edge.attr;
		}).collect();
		Lane[] lanes = new Lane[links.size()];
		for(int i = 0; i<lanes.length; i++) {
			Link l = links.get(i);
			lanes[i] = new Lane(Coordinate2Point(l.getHead().getCoordinate()), 
					Coordinate2Point(l.getTail().getCoordinate()));
		}
        return lanes;
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
