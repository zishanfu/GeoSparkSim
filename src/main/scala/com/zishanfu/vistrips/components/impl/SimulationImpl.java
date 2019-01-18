package com.zishanfu.vistrips.components.impl;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import com.zishanfu.vistrips.model.Vehicle;

public class SimulationImpl {
	private final Logger LOG = Logger.getLogger(SimulationImpl.class);
	private SparkSession spark;
	
	public SimulationImpl(SparkSession spark) {
		this.spark = spark;
	}
	
	public void apply(JavaRDD<Vehicle> vehicles, double delayInSec, int routeLength) {
		
		SpatialRDD<Vehicle> sRDD = new SpatialRDD();
		sRDD.setRawSpatialRDD(vehicles);	
		sRDD.analyze();

		try {
			sRDD.spatialPartitioning(GridType.QUADTREE, 8);
			//sRDD.buildIndex(IndexType.QUADTREE, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int delay = (int)(delayInSec*1000);
		
		
		Timer timer = new Timer();
	
		//timer.schedule(new SimTask(sRDD.spatialPartitionedRDD, timer, routeLength), 0, delay);
		
	}

}
