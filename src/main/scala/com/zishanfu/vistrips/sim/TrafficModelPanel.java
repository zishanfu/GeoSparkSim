package com.zishanfu.vistrips.sim;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.graphx.EdgeRDD;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.enums.IndexType;
import org.datasyslab.geospark.spatialOperator.JoinQuery;
import org.datasyslab.geospark.spatialRDD.PolygonRDD;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.zishanfu.vistrips.model.Link;
import com.zishanfu.vistrips.sim.model.IDMVehicle;
import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.model.Point;
import com.zishanfu.vistrips.sim.model.Segment;
import com.zishanfu.vistrips.sim.ui.MapWindow;

import scala.Tuple2;


public class TrafficModelPanel{
	private World world;
	private final Logger LOG = Logger.getLogger(TrafficModelPanel.class);
	final static GeometryFactory gf = new GeometryFactory();
	private static List<List<Coordinate>> trajectories = new ArrayList<>();
	private static int iterations = 1;
	
	public TrafficModelPanel(World world) {
		this.world = world;
	}
	
	public void run(int minutes) throws Exception {
		OsmGraph osmGraph = world.graph();
        EdgeRDD<Link> edges = osmGraph.graph().edges();
        
        
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

		//paint signals
		System.out.println(osmGraph.getSignals().length);
		window.addSignals(osmGraph.getSignals());
		
		iterations = (int) (minutes*60 / 2);

		int shuffleSlot = 2 * 60; //10minutes
		JavaRDD<IDMVehicle> rawVehicles = world.roadVehicles().toJavaRDD();
		
		if(shuffleSlot < minutes*60) {
			//caculate initial shuffle
			JavaRDD<IDMVehicle> shuffledVehicles = rawVehicles.map(vehicle -> {
				long time = vehicle.getTime();
				if(time < shuffleSlot) {
					return vehicle;
				}else {
					Coordinate[] coordinates = vehicle.initialShuffle(shuffleSlot);
					IDMVehicle veh = new IDMVehicle(coordinates, vehicle.getSRID(), time, vehicle.getDistance());
					return veh;
				}
			});
		}

		SpatialRDD<IDMVehicle> vehicleRDD = new SpatialRDD<IDMVehicle>();
		vehicleRDD.setRawSpatialRDD(rawVehicles);
		vehicleRDD.analyze();
		vehicleRDD.spatialPartitioning(GridType.QUADTREE, 8);
		vehicleRDD.buildIndex(IndexType.QUADTREE, true);
		
		vehicleIterator(vehicleRDD, 0);
		System.out.print(trajectories.get(0).get(0));
		for(List<Coordinate> trajectory: trajectories) {
			for(Coordinate coordinate: trajectory) {
				System.out.print(coordinate + ",");
			}
			System.out.print("---");
		}
		
    }
	
	//2 seconds
	private static void vehicleIterator(SpatialRDD<IDMVehicle> vehicles, int i) throws Exception {
		if(i >= iterations) return;
		
		//join buffer
		boolean considerBoundaryIntersection = false;
		boolean usingIndex = true;
		JavaRDD<Polygon> buffers = vehicles.rawSpatialRDD.map(veh -> {
			Polygon poly = null;
			if(veh.getvBuffer() == null) {
				poly = veh.getSelf();
			}else {
				poly = veh.getvBuffer().getHead();
			}
			poly.setUserData(veh);
			return poly;
		});
		
		PolygonRDD windows = new PolygonRDD(buffers);
		windows.analyze();
		windows.spatialPartitioning(vehicles.getPartitioner());
		
		JavaPairRDD<Polygon, HashSet<IDMVehicle>> results = JoinQuery.SpatialJoinQuery(vehicles, windows, usingIndex, considerBoundaryIntersection);
		JavaPairRDD<Polygon, HashSet<IDMVehicle>> filteredResults = results.mapToPair(tuple -> {
			Polygon p = tuple._1;
			HashSet<IDMVehicle> set = tuple._2;
			for(IDMVehicle veh: set) {
				Coordinate curMove = veh.getLocation();
				com.vividsolutions.jts.geom.Point curPoint = gf.createPoint(curMove);
				if(!curPoint.within(p)) {
					set.remove(veh);
				}
			}
			set.remove((IDMVehicle)p.getUserData());
			return new Tuple2<Polygon, HashSet<IDMVehicle>>(p,set);
		});
		
		JavaRDD<IDMVehicle> detectedVehicles = filteredResults.map(tuple -> {
			IDMVehicle veh = (IDMVehicle) tuple._1.getUserData();
			veh.setAheadVehicles(tuple._2);
			return veh;
		});
		List<Coordinate> list = new ArrayList<>();
		JavaRDD<IDMVehicle> movedVehicle = detectedVehicles.map(veh -> {
			Coordinate next = veh.moveNext();
			list.add(next);
			veh.setLocation(next);
			return veh;
		});
		trajectories.add(list);
		vehicles.setRawSpatialRDD(movedVehicle);
		vehicleIterator(vehicles, i+1);
	}
	
}
