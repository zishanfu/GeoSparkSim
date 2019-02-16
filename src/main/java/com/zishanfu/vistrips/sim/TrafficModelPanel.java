package com.zishanfu.vistrips.sim;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.zishanfu.vistrips.sim.model.VehicleRectangle;
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
		long repartTime = 0;
		LOG.warn("Partition iteration begin...");
		
		for(int part = 0; part < repartition; part++) {
			
			long t3 = System.currentTimeMillis();
			
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
			long t4 = System.currentTimeMillis();

			LOG.warn("Repartition Vehicles " + scount);
			repartTime += (t4-t3) / 1000;
			
			JavaRDD<Report> reportRDD = vehicleRDD.spatialPartitionedRDD.mapPartitions(vehicles -> {
				List<Report> reports = new ArrayList<>();
				Iterator<IDMVehicle> iterator = vehicles;
				
				for(int i = 0; i<shuffleSlot/timestamp; i++) {
					List<VehicleRectangle> vBuffers = new ArrayList<>();
					Map<Coordinate, IDMVehicle> map = new HashMap<>();
					
					while(iterator.hasNext()) {
						IDMVehicle veh = iterator.next();
						//find buffer
						double[] headBuffer = veh.getvBuffer() == null ? veh.getSelf():veh.getvBuffer().getHead();
						VehicleRectangle rect = new VehicleRectangle(headBuffer);
						rect.setVehicle(veh);
						vBuffers.add(rect);
						map.put(veh.getLocation(), veh);
					}
					
					List<IDMVehicle> bufferedVehicles = new ArrayList<>();
					
					for(VehicleRectangle buffer: vBuffers) {
						Set<IDMVehicle> set = new HashSet<>();
						for(Coordinate cur: map.keySet()) {
							double[] rect = buffer.getRectangle(); //left, right, top, bottom
							if(cur.x > rect[0] && cur.x < rect[1] && cur.y < rect[2] && cur.y > rect[3]) {
								set.add(map.get(cur));
							}
						}
						IDMVehicle bVeh = buffer.getVehicle();
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
					iterator = bufferedVehicles.iterator();
				}
				return reports.iterator();
			});

			reportRDD.saveAsTextFile(hdfs.getHDFSUrl() + "/vistrips/reports_" + part);
		}
		
		long t5 = System.currentTimeMillis();
		LOG.warn(String.format("Finished Simulation! Simulation Total: %s seconds, Repartition Total: %s seconds", (t5-t2) / 1000, repartTime));
		
    }

}
