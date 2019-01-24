package com.zishanfu.vistrips.sim;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.enums.IndexType;
import org.datasyslab.geospark.spatialOperator.JoinQuery;
import org.datasyslab.geospark.spatialRDD.PolygonRDD;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.zishanfu.vistrips.sim.model.IDMVehicle;

import scala.Tuple2;


public class TrafficModelPanel{
	private World world;
	private final Logger LOG = Logger.getLogger(TrafficModelPanel.class);
	final static GeometryFactory gf = new GeometryFactory();
	private static int iterations = 1;
	
	public TrafficModelPanel(World world) {
		this.world = world;
	}
	
	public void run(double simTime) throws Exception {
//		OsmGraph osmGraph = world.graph();
//        EdgeRDD<Link> edges = osmGraph.graph().edges();

//        MapWindow window = new MapWindow();
//        window.setVisible(true);
//
//        List<Link> links = edges.toJavaRDD().map(edge ->{
//			return edge.attr;
//		}).collect();
//
//		for(int i = 0; i<links.size(); i++) {
//			Link l = links.get(i);
//			Point head = new Point(l.getHead().getCoordinate().y, l.getHead().getCoordinate().x);
//			Point tail = new Point(l.getTail().getCoordinate().y, l.getTail().getCoordinate().x);
//			window.addSegment(new Segment(head, tail, Color.GRAY, l.getLanes()));
//		}
//
//		window.addSignals(osmGraph.getSignals());
		
		iterations = (int) (simTime*60 / 2);

		int shuffleSlot = 2 * 60; //10minutes
		JavaRDD<IDMVehicle> rawVehicles = world.roadVehicles().toJavaRDD();
		
//		if(shuffleSlot < simTime*60) {
//			//caculate initial shuffle
//			JavaRDD<IDMVehicle> shuffledVehicles = rawVehicles.map(vehicle -> {
//				long time = vehicle.getTime();
//				if(time < shuffleSlot) {
//					return vehicle;
//				}else {
//					Coordinate[] coordinates = vehicle.initialShuffle(shuffleSlot);
//					IDMVehicle veh = new IDMVehicle(coordinates, vehicle.getSRID(), time, vehicle.getDistance());
//					return veh;
//				}
//			});
//		}

		SpatialRDD<IDMVehicle> vehicleRDD = new SpatialRDD<IDMVehicle>();
		vehicleRDD.setRawSpatialRDD(rawVehicles);
		vehicleRDD.analyze();
		vehicleRDD.spatialPartitioning(GridType.QUADTREE, 8);
		vehicleRDD.buildIndex(IndexType.QUADTREE, true);
		boolean considerBoundaryIntersection = true;
		boolean usingIndex = true;
		
		for(int i = 0; i<iterations; i++) {
			//join buffer
			PolygonRDD windows = new PolygonRDD(
					vehicleRDD.rawSpatialRDD.map(veh -> {
						Polygon poly = null;
						if(veh.getvBuffer() == null) {
							poly = veh.getSelf();
						}else {
							poly = veh.getvBuffer().getHead();
						}
						poly.setUserData(veh);
						return poly;
					}));
			
			windows.analyze();
			windows.spatialPartitioning(vehicleRDD.getPartitioner());
			
//			windows.rawSpatialRDD.take(10).forEach(System.out::println);
//			vehicleRDD.rawSpatialRDD.take(10).forEach(System.out::println);
			
			JavaPairRDD<Polygon, HashSet<IDMVehicle>> tmp = JoinQuery
					.SpatialJoinQuery(vehicleRDD, windows, usingIndex, considerBoundaryIntersection);
//			tmp.take(10).forEach(System.out::println);
			
			JavaPairRDD<Polygon, HashSet<IDMVehicle>> results = tmp
					.mapToPair(tuple -> {
						Polygon p = tuple._1;
						HashSet<IDMVehicle> set = new HashSet<>();
						for(IDMVehicle veh: tuple._2) {
							Coordinate curMove = veh.getLocation();
							com.vividsolutions.jts.geom.Point curPoint = gf.createPoint(curMove);
							if(curPoint.within(p)) {
								set.add(veh);
							}
						}
						set.remove((IDMVehicle)p.getUserData());
						return new Tuple2<Polygon, HashSet<IDMVehicle>>(p,set);
					});
//			results.take(10).forEach(System.out::println);
			List<Coordinate> list = new ArrayList<>();
			JavaRDD<IDMVehicle> movedVehicle = results.map(tuple -> {
									IDMVehicle veh = (IDMVehicle) tuple._1.getUserData();
									veh.setAheadVehicles(tuple._2);
									return veh;
								}).map(veh -> {
									Coordinate next = veh.moveNext();
									veh.setLocation(next, veh.getNextIdx());
									return veh;
								});
//			movedVehicle.take(10).forEach(mv -> {
//				System.out.println(mv.getLocation());
//			});
			
			vehicleRDD.setRawSpatialRDD(movedVehicle);
		}
		
		System.out.print("Finished Simulation!");
		LOG.warn("Finished Simulation!");
		
//		for(List<Coordinate> trajectory: trajectories) {
//			for(Coordinate coordinate: trajectory) {
//				System.out.print(coordinate + ",");
//			}
//			System.out.print("---");
//		}
		
    }

}
