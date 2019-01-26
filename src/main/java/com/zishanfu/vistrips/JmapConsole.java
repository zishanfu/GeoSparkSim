package com.zishanfu.vistrips;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.components.impl.GenerationImpl;
import com.zishanfu.vistrips.components.impl.SimulationImpl;
import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.model.IDMVehicle;
import com.zishanfu.vistrips.tools.HDFSUtil;

public class JmapConsole {
	private final static Logger LOG = Logger.getLogger(JmapConsole.class);
	private final Properties prop = new Properties();
	private String filename = "app.config";
	private InputStream is = null;
	private SparkSession spark;
	private JavaRDD<IDMVehicle> vehicles;
	private OsmGraph graph;
	private HDFSUtil hdfs;
	private String osm;
	private int cores;
	
	public JmapConsole(SparkSession spark, HDFSUtil hdfs, String osm, int cores) {
		this.spark = spark;
		this.hdfs = hdfs;
		this.osm = osm;
		this.cores = cores;
	}
	
	public void runGeneration(int total) {
//		try {
//		    is = new FileInputStream(resources + "/config/" + filename);
//		} catch (FileNotFoundException ex) {
//		    LOG.error("app.config file can't be found");
//		}
//		try {
//		    prop.load(is);
//		} catch (IOException ex) {
//			LOG.error("app.config file can't be load");
//		}

//		String appName = prop.getProperty("app.name");
//		String appVersion = prop.getProperty("app.version");
//		double geo1Lat = Double.parseDouble(prop.getProperty("geo1.lat"));
//		double geo1Lon = Double.parseDouble(prop.getProperty("geo1.lon"));
//		double geo2Lat = Double.parseDouble(prop.getProperty("geo2.lat"));
//		double geo2Lon = Double.parseDouble(prop.getProperty("geo2.lon"));
		
		double geo1Lat = 33.48998;
		double geo1Lon = -112.10964;
		double geo2Lat = 33.38827;
		double geo2Lon = -111.79722;
		
		GeoPosition geo1 = new GeoPosition(geo1Lat, geo1Lon);
		GeoPosition geo2 = new GeoPosition(geo2Lat, geo2Lon);
		//String selectedType = typeParser(prop.getProperty("generation.type"));
		String selectedType = "DSO";
//		int total = Integer.parseInt(prop.getProperty("generation.num"));
		
		GenerationImpl gImpl = new GenerationImpl(spark, hdfs, cores);
		this.vehicles = gImpl.apply(geo1, geo2, selectedType, total, osm);
		
		long t1 = System.currentTimeMillis();
		this.graph = new OsmGraph(spark, gImpl.getPath());
		long t2 = System.currentTimeMillis();
		LOG.warn(String.format("Road Network Construction! Time: %s seconds", (t2-t1) / 1000));
	}
	
	public void runSimulation(double timestamp, double simTime, double partitionTime) {
//		double timestamp = Double.parseDouble(prop.getProperty("simulation.timestamp"));
//		double simTime = Double.parseDouble(prop.getProperty("simulation.minutes"));
//		double partitionTime = Double.parseDouble(prop.getProperty("simulation.partitiontime"));
		SimulationImpl sImpl = new SimulationImpl(vehicles, graph, cores);
		sImpl.apply(simTime, partitionTime, timestamp, hdfs);
	}

	private String typeParser(String type) {
		switch(type) {
		case "data-space oriented approach":
			return "DSO";
		case "region-based approach":
			return "RB";
		case "network-based approach":
			return "RB";
		default:
			return type;
		}
	}
}
