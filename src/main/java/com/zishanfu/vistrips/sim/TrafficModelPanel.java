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
	private int partition;
	
	public TrafficModelPanel(World world, HDFSUtil hdfs, int partition) {
		this.world = world;
		this.hdfs = hdfs;
		this.partition = partition;
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
		vehicleRDD.spatialPartitioning(GridType.KDBTREE, partition);
		
		long t2 = System.currentTimeMillis();
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

			vehicleRDD.setRawSpatialRDD(shuffledVehicles);
			vehicleRDD.analyze();
			vehicleRDD.spatialPartitioning(GridType.KDBTREE, partition);
			
			long scount = vehicleRDD.spatialPartitionedRDD.count();
			long t3 = System.currentTimeMillis();
			//repartition
			LOG.warn("Repartition shuffledVehicles " + scount + " Time: " + (t3-t2) / 1000);

			
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

			reportRDD.saveAsTextFile(hdfs.getHDFSUrl() + "/vistrips/reports_" + part);
			long t4 = System.currentTimeMillis();
			LOG.warn(String.format("Finished Partition %s Simulation! Time: %s seconds", part, (t4-t3) / 1000));
		}

		
    }

}
