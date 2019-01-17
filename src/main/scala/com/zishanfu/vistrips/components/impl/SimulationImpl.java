package com.zishanfu.vistrips.components.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.enums.IndexType;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import com.zishanfu.vistrips.model.NewWaypoint;
import com.zishanfu.vistrips.model.Pair;

public class SimulationImpl {
	private final Logger LOG = Logger.getLogger(SimulationImpl.class);
	private SparkSession spark;
	
	public SimulationImpl(SparkSession spark) {
		this.spark = spark;
	}
	
	public void apply(Pair[] pairs, double delayInSec, int routeLength) {
		List<NewWaypoint> list = new ArrayList<>();
		
		for(Pair p: pairs) {
			if(p == null) {
				continue;
			}
			list.add(new NewWaypoint(p.getCoordinates(), p.getPrecisionModel(), p.getSRID()));
		}
		JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
		
		JavaRDD<NewWaypoint> wRDD = sc.parallelize(list);
		SpatialRDD<NewWaypoint> sRDD = new SpatialRDD();
		sRDD.setRawSpatialRDD(wRDD);	
		sRDD.analyze();

		try {
			sRDD.spatialPartitioning(GridType.QUADTREE, 10);
			sRDD.buildIndex(IndexType.QUADTREE, false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int delay = (int)(delayInSec*1000);
		
		
		Timer timer = new Timer();
	
		timer.schedule(new SimTask(sRDD.spatialPartitionedRDD, timer, routeLength), 0, delay);
		
	}

}
