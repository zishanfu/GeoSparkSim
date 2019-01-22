package com.zishanfu.vistrips.components.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.zishanfu.vistrips.model.Vehicle;
import com.zishanfu.vistrips.osm.GraphInit;
import com.zishanfu.vistrips.osm.OsmParser;
import com.zishanfu.vistrips.tools.Distance;
import com.zishanfu.vistrips.tools.FileOps;
import com.zishanfu.vistrips.tools.SpatialRandom;
import com.zishanfu.vistrips.tools.Utils;

public class GenerationImpl2 implements Serializable{

	private final static Logger LOG = Logger.getLogger(GenerationImpl.class);
	private SparkSession spark;
	private PrecisionModel precision = new PrecisionModel();
	private String mapPath;
	
	//testing
	
	public GenerationImpl2(SparkSession spark) {
		this.spark = spark;
	}
	
	public String getMapPath() {
		return mapPath;
	}

	public JavaRDD<Vehicle> apply(GeoPosition geo1, GeoPosition geo2, String generationType, int total) {
		//scale the length of trip, same with the scale in trip generation
		double maxLen = new Distance().euclidean(geo1, geo2) / 10; 
		
		//Add a download buffer
		GeoPosition newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen);
		GeoPosition newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen);
		//System.out.println(String.format("Selected rectangle, p1: %s, p2: %s", newGeo1, newGeo2));

		//String path = OsmParser.run(newGeo1, newGeo2);
		LOG.warn("Downloading selected OSM data ...");
		//Download osm pn disk 
		String osmPath = osmDownloader(newGeo1, newGeo2);
		
		//Parser osm to parquet on hdfs
		mapPath = new OsmParser().runInLocal(newGeo1, newGeo2);
		
		LOG.warn(String.format("Finished download! Data located in ( %s )", osmPath));
		
		LOG.warn("Processing graph ...");
		GraphInit gi = new GraphInit(osmPath);
		//OsmGraph graph = new OsmGraph(spark, path);
		LOG.warn("Finished graph construction");
		
		LOG.warn(String.format("Begin generate %s trips.", total));
		JavaRDD<Vehicle> vRDD = vehicleGeneration(geo1, geo2, gi, maxLen, generationType, total);
		LOG.warn("Finished!");
		return vRDD;
	}
	
	public String osmDownloader(GeoPosition geo1, GeoPosition geo2) {
		String OSM_URL = "http://overpass-api.de/api/map?bbox=";
		String pathBase = System.getProperty("user.dir") + "/src/test/resources/vistrips";
		URL url = null;
		
		double left = Math.min(geo1.getLongitude(), geo2.getLongitude());
		double right = Math.max(geo1.getLongitude(), geo2.getLongitude());
		double top = Math.max(geo1.getLatitude(), geo2.getLatitude());
		double bottom = Math.min(geo1.getLatitude(), geo2.getLatitude());
		//"-111.93000,33.41237,-111.92175,33.41732"
		
		OSM_URL += left + "," + bottom + "," + right + "," + top;
		
		new FileOps().createDirectory(pathBase);

		try {
			url = new URL(OSM_URL);
		} catch (MalformedURLException e) {
			LOG.error("MalformedURLException for Bbx download format");
			e.printStackTrace();
		}
		
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String newFileName = String.format("%s/%s.osm", pathBase, dateFormat.format(now));
		
		try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(newFileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            File osm = new File(newFileName);
            LOG.warn(osm.length() / (1024 * 1024) + "MB");
            return newFileName;
        } catch (IOException e) {
        	LOG.error("IOException for downloading osm");
        	e.printStackTrace();
        	return null;
        }
	}
	
	private JavaRDD<Vehicle> vehicleGeneration(GeoPosition topleft, GeoPosition bottomright, GraphInit graph, double maxLen, String type, int total) {
		double minLat = Math.min(topleft.getLatitude(), bottomright.getLatitude());
		double maxLat = Math.max(topleft.getLatitude(), bottomright.getLatitude());
		double minLon = Math.min(topleft.getLongitude(), bottomright.getLongitude());
		double maxLon = Math.max(topleft.getLongitude(), bottomright.getLongitude());
		long totalNodes = graph.getTotalNodes();
		SpatialRandom spatialRand = new SpatialRandom( minLon, minLat, maxLon, maxLat, maxLen, graph);
		
		JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
		int partitions = 8;
		int recordsPerPartitions = (int) Math.ceil((double)total/partitions);
		Vehicle[] vehicleArr = new Vehicle[total];
		
		for(int i = 0; i<total; i++) {
			vehicleArr[i] = computeVehicle(type, spatialRand);
		}
		
		JavaRDD<Vehicle> vehicles = sc.parallelize(Arrays.asList(vehicleArr), partitions);
		

		return vehicles;
	}
	
	private Vehicle computeVehicle(String type, SpatialRandom rand){
		double len = rand.spatialRandomLen();
		Coordinate src = null;
		Coordinate dest = null;
		if(type.contains("DSO")) {
			src = rand.spatialRandomNode();
			dest = rand.computeDestDSO(src, len);
		}else if(type.contains("NB")) {
			src = rand.computeSourceNB();
			dest = rand.computeDestinationNB(src, len);
		}else if(type.contains("RB")) {
			
		}	

		PathWrapper path = rand.getPath(src, dest);
		//recompute to meet the total requests
		if(path == null) {
			return computeVehicle(type, rand);
		}
		
		PointList route = path.getPoints();
		if(route == null || route.size() <= 1) {
			return computeVehicle(type, rand);
		}
			
		Coordinate[] routeCoordinates = new Utils().toCoordinates(route);
		//LineString routeInStep = new Interpolate().routeInterpolateBySec(lsRoute, path.getTime()/1000, path.getDistance(), 1.0);
		
		Vehicle vehicle = new Vehicle(routeCoordinates);
		vehicle.apply(src, dest, path.getDistance(), path.getTime()/1000);

		return vehicle;		
	}

}
