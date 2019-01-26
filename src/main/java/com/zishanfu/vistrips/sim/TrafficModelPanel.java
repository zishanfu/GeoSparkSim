package com.zishanfu.vistrips.sim;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.zishanfu.vistrips.sim.model.IDMVehicle;
import com.zishanfu.vistrips.sim.model.Report;
import com.zishanfu.vistrips.sim.model.World;
import com.zishanfu.vistrips.tools.HDFSUtil;


public class TrafficModelPanel{
	private World world;
	private final Logger LOG = Logger.getLogger(TrafficModelPanel.class);
	final static GeometryFactory gf = new GeometryFactory();
	private static int iterations = 1;
	private static final String resources = System.getProperty("user.dir") + "/src/test/resources";
	private HDFSUtil hdfs;
	private int cores;
	
	public TrafficModelPanel(World world, HDFSUtil hdfs, int cores) {
		this.world = world;
		this.hdfs = hdfs;
		this.cores = cores;
	}
	
	public void run(double simTime, double partitionTime, double timestamp) throws Exception {
		long t1 = System.currentTimeMillis();
		LOG.warn("Simulation begin...");
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
		
		iterations = (int) (simTime*60 / timestamp);
		int repartition = (int) (partitionTime >= simTime? 1: simTime/partitionTime);

		JavaRDD<IDMVehicle> rawVehicles = world.getRoadVehicles();
		
		int shuffleSlot = (int) (partitionTime*60);

		
		SpatialRDD<IDMVehicle> vehicleRDD = new SpatialRDD<IDMVehicle>();
		vehicleRDD.setRawSpatialRDD(rawVehicles);
		vehicleRDD.analyze();
		vehicleRDD.spatialPartitioning(GridType.QUADTREE, cores);
		
		LOG.warn("Partition iteration begin...");
		
		for(int part = 0; part < repartition; part++) {
			
			JavaRDD<IDMVehicle> shuffledVehicles = part == 0?
			vehicleRDD.getRawSpatialRDD().map(vehicle -> {
				long time = vehicle.getTime();
				if(time < shuffleSlot) {
					return vehicle;
				}else {	
					Coordinate[] coordinates = vehicle.initialShuffle(shuffleSlot);
					IDMVehicle veh = new IDMVehicle(coordinates, vehicle.getSRID(), time, vehicle.getDistance());
					return veh;
				}
			}) : vehicleRDD.getRawSpatialRDD().map(vehicle -> {
				long time = vehicle.getTime();
				if(time < shuffleSlot) {
					return vehicle;
				}else {	
					Coordinate[] coordinates = vehicle.nextShuffle(time, vehicle.getLocation());
					IDMVehicle veh = new IDMVehicle(coordinates, vehicle.getSRID(), time, vehicle.getDistance());
					return veh;
				}
			});
			
			
			//repartition
			LOG.warn("Finished redefine linestring coordinates and repartition...shuffledVehicles " + shuffledVehicles.count());
			
			vehicleRDD.setRawSpatialRDD(shuffledVehicles);
			vehicleRDD.analyze();
			vehicleRDD.spatialPartitioning(GridType.QUADTREE, cores);
			
			JavaRDD<Report> reportRDD = vehicleRDD.spatialPartitionedRDD.mapPartitions(vehicles -> {
				List<Report> reports = new ArrayList<>();
				Iterator<IDMVehicle> iterator = vehicles;
				
//				LOG.warn("Begin iteration" + (shuffleSlot/timestamp));
				
				for(int i = 0; i<shuffleSlot/timestamp; i++) {
					
					List<Polygon> vBuffers = new ArrayList<>();
					List<com.vividsolutions.jts.geom.Point> curVehicles = new ArrayList<>();
					while(iterator.hasNext()) {
						IDMVehicle veh = iterator.next();
						//find buffer
						Polygon headBuffer = veh.getvBuffer() == null ? veh.getSelf():veh.getvBuffer().getHead();
						headBuffer.setUserData(veh);
						vBuffers.add(headBuffer);
						com.vividsolutions.jts.geom.Point location = gf.createPoint(veh.getLocation());
						location.setUserData(veh);
						curVehicles.add(location);
					}
					List<IDMVehicle> bufferedVehicles = new ArrayList<>();
					
//					LOG.warn("Created Vehicles and Vehicle Buffers. Begin check...");
					
					for(Polygon buffer: vBuffers) {
						Set<IDMVehicle> set = new HashSet<>();
						for(com.vividsolutions.jts.geom.Point cur: curVehicles) {
							if(cur.within(buffer)) {
								set.add((IDMVehicle)cur.getUserData());
							}
						}
						IDMVehicle bVeh = (IDMVehicle)buffer.getUserData();
						bVeh.setAheadVehicles(set);
						Coordinate next = bVeh.moveNext();
						//true -> didn't arrive
						//false -> arrive -> reborn the vehicle
						if(bVeh.setLocation(next, bVeh.getNextIdx())) {
							Report rep = bVeh.getReport();
							rep.setSid(i);
							reports.add(rep);
						}else {
							bVeh = new IDMVehicle(bVeh.getCoordinates(), bVeh.getSRID(), bVeh.getTime(), bVeh.getDistance());
						}
						bufferedVehicles.add(bVeh);
					}
//					LOG.warn("Finished all vehicle checks and move forward...");
					iterator = bufferedVehicles.iterator();
				}
				return reports.iterator();
			});
			
			long t2 = System.currentTimeMillis();
			LOG.warn(String.format("Before Write File! Time: %s seconds", (t2-t1) / 1000));
			reportRDD.saveAsTextFile(hdfs.getHDFSUrl() + "/vistrips/reports_" + part);
			long t3 = System.currentTimeMillis();
			LOG.warn(String.format("Finished Partition %s Simulation! Time: %s seconds", part, (t3-t2) / 1000));
		}
		
		
//		for(int i = 0; i<iterations; i++) {
//			//join buffer
//			PolygonRDD windows = new PolygonRDD(
//					vehicleRDD.spatialPartitionedRDD.map(veh -> {
//						Polygon poly = null;
//						if(veh.getvBuffer() == null) {
//							poly = veh.getSelf();
//						}else {
//							poly = veh.getvBuffer().getHead();
//						}
//						poly.setUserData(veh);
//						return poly;
//					}));
//			
//			windows.analyze();
//			windows.spatialPartitioning(vehicleRDD.getPartitioner());
//			
//			JavaPairRDD<Polygon, HashSet<IDMVehicle>> tmp = JoinQuery
//					.SpatialJoinQuery(vehicleRDD, windows, usingIndex, considerBoundaryIntersection);
//			
//			JavaPairRDD<Polygon, HashSet<IDMVehicle>> results = tmp
//					.mapToPair(tuple -> {
//						Polygon p = tuple._1;
//						HashSet<IDMVehicle> set = new HashSet<>();
//						for(IDMVehicle veh: tuple._2) {
//							Coordinate curMove = veh.getLocation();
//							com.vividsolutions.jts.geom.Point curPoint = gf.createPoint(curMove);
//							if(curPoint.within(p)) {
//								set.add(veh);
//							}
//						}
//						set.remove((IDMVehicle)p.getUserData());
//						return new Tuple2<Polygon, HashSet<IDMVehicle>>(p,set);
//					});
//
//			JavaRDD<IDMVehicle> movedVehicle = results.map(tuple -> {
//									IDMVehicle veh = (IDMVehicle) tuple._1.getUserData();
//									veh.setAheadVehicles(tuple._2);
//									return veh;
//								}).map(veh -> {
//									Coordinate next = veh.moveNext();
//									veh.setLocation(next, veh.getNextIdx());
//									return veh;
//								});
//			
//			vehicleRDD.setRawSpatialRDD(movedVehicle);
//		}
		
		
		
//		for(List<Coordinate> trajectory: trajectories) {
//			for(Coordinate coordinate: trajectory) {
//				System.out.print(coordinate + ",");
//			}
//			System.out.print("---");
//		}
		
    }

}
