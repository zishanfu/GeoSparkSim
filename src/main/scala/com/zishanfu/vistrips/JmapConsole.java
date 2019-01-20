package com.zishanfu.vistrips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.components.impl.GenerationImpl;
import com.zishanfu.vistrips.components.impl.GenerationImpl2;
import com.zishanfu.vistrips.components.impl.SimulationImpl;
import com.zishanfu.vistrips.model.Vehicle;

public class JmapConsole {
	private final static Logger LOG = Logger.getLogger(JmapConsole.class);
	private final Properties prop = new Properties();
	private String filename = "app.config";
	private InputStream is = null;
	private SparkSession spark;
	private String resources;
	private JavaRDD<Vehicle> vehicles;
	private double tripTime;
	
	public JmapConsole(String resources, SparkSession spark) {
		this.spark = spark;
		this.resources = resources;
	}
	
	public void runGeneration() {
		try {
		    is = new FileInputStream(resources + "/config/" + filename);
		} catch (FileNotFoundException ex) {
		    LOG.error("app.config file can't be found");
		}
		try {
		    prop.load(is);
		} catch (IOException ex) {
			LOG.error("app.config file can't be load");
		}

		String appName = prop.getProperty("app.name");
		String appVersion = prop.getProperty("app.version");
		double geo1Lat = Double.parseDouble(prop.getProperty("geo1.lat"));
		double geo1Lon = Double.parseDouble(prop.getProperty("geo1.lon"));
		double geo2Lat = Double.parseDouble(prop.getProperty("geo2.lat"));
		double geo2Lon = Double.parseDouble(prop.getProperty("geo2.lon"));
		LOG.info("");
		GeoPosition geo1 = new GeoPosition(geo1Lat, geo1Lon);
		GeoPosition geo2 = new GeoPosition(geo2Lat, geo2Lon);
		String selectedType = typeParser(prop.getProperty("generation.type"));
		int total = Integer.parseInt(prop.getProperty("generation.num"));
		
		GenerationImpl2 gImpl = new GenerationImpl2(spark);
		vehicles = gImpl.apply(geo1, geo2, selectedType, total);
	}
	
	public void runSimulation() {
		double timestamp = Double.parseDouble(prop.getProperty("simulation.timestamp"));
		double simTime = Double.parseDouble(prop.getProperty("simulation.minutes"));
		SimulationImpl sImpl = new SimulationImpl(spark);
		sImpl.apply(vehicles, timestamp, simTime);
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
