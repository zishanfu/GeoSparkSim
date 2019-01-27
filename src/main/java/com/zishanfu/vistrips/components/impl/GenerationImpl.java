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
import com.zishanfu.vistrips.osm.GraphInit;
import com.zishanfu.vistrips.osm.OsmParser;
import com.zishanfu.vistrips.sim.model.IDMVehicle;
import com.zishanfu.vistrips.tools.Distance;
import com.zishanfu.vistrips.tools.FileOps;
import com.zishanfu.vistrips.tools.HDFSUtil;
import com.zishanfu.vistrips.tools.SpatialRandom;
import com.zishanfu.vistrips.tools.Utils;

public class GenerationImpl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3340548967515954862L;
	private final static Logger LOG = Logger.getLogger(GenerationImpl.class);
	private SparkSession spark;
	private String path;
	private HDFSUtil hdfs;
	private int partition;
	
	//testing
	
	public GenerationImpl(SparkSession spark, HDFSUtil hdfs, int partition) {
		this.spark = spark;
		this.hdfs = hdfs;
		this.partition = partition;
	}
	

	public String getPath() {
		return path;
	}


	public JavaRDD<IDMVehicle> apply(GeoPosition geo1, GeoPosition geo2, String generationType, int total, String local) {
		long t1 = System.currentTimeMillis();
		//scale the length of trip, same with the scale in trip generation
		double maxLen = new Distance().euclidean(geo1, geo2) / 10; 
		
		//Add a download buffer
		GeoPosition newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen);
		GeoPosition newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen);
		//System.out.println(String.format("Selected rectangle, p1: %s, p2: %s", newGeo1, newGeo2));

		//String path = OsmParser.run(newGeo1, newGeo2);
		LOG.warn("Processing OSM data in GraphX");
		
		//Parser osm to parquet on hdfs
		//mapPath = new OsmParser().runInLocal(newGeo1, newGeo2);
		path = new OsmParser(hdfs).runInHDFS(newGeo1, newGeo2);
		
		//Download osm pn disk 
		//String local = osmDownloader(newGeo1, newGeo2);
		long t2 = System.currentTimeMillis();
		
		//String osmPath = HDFSUtil.uploadLocalFile2HDFS(local, hdfs);
		
		LOG.warn(String.format("Finished OSM graph construction! Time: %s . Processing graphhopper ...", (t2-t1) / 1000));
		
		GraphInit gi = new GraphInit(local);
		//OsmGraph graph = new OsmGraph(spark, path);
		long t3 = System.currentTimeMillis();
		LOG.warn(String.format("Finished graphhopper construction. Time: %s.", (t3-t2) / 1000));
		
		LOG.warn(String.format("Begin generate %s trips.", total));
		JavaRDD<IDMVehicle> vRDD = vehicleGeneration(geo1, geo2, gi, maxLen, generationType, total);
		
		long t4 = System.currentTimeMillis();
		LOG.warn(String.format("Finished Generation! Time: %s seconds", (t4-t3) / 1000));
		return vRDD;
	}
	
	private String osmDownloader(GeoPosition geo1, GeoPosition geo2) {
		String OSM_URL = "http://overpass-api.de/api/map?bbox=";
		String pathBase = System.getProperty("user.dir") + "/src/test/resources/vistrips";
//		String pathBase = HDFSUtil.hdfsUrl() + "/vistrips";
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss'Z'");
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
	
	@SuppressWarnings("resource")
	private JavaRDD<IDMVehicle> vehicleGeneration(GeoPosition topleft, GeoPosition bottomright, GraphInit graph, double maxLen, String type, int total) {
		double minLat = Math.min(topleft.getLatitude(), bottomright.getLatitude());
		double maxLat = Math.max(topleft.getLatitude(), bottomright.getLatitude());
		double minLon = Math.min(topleft.getLongitude(), bottomright.getLongitude());
		double maxLon = Math.max(topleft.getLongitude(), bottomright.getLongitude());
		long totalNodes = graph.getTotalNodes();
		SpatialRandom spatialRand = new SpatialRandom( minLon, minLat, maxLon, maxLat, maxLen, graph);
		
		JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

		IDMVehicle[] vehicleArr = new IDMVehicle[10000];
		
		for(int i = 0; i<10000; i++) {
			vehicleArr[i] = computeVehicle(type, spatialRand, i);
		}
		
		int union = total/10000;
		
		JavaRDD<IDMVehicle> vehicles = sc.parallelize(Arrays.asList(vehicleArr), partition);
		
		JavaRDD<IDMVehicle> totalVehicles = vehicles;
				
		for(int i = 0; i<union-1; i++){
			totalVehicles = totalVehicles.union(vehicles);
	    }
		
		totalVehicles.repartition(partition);
		
		return totalVehicles;
	}
	
	private IDMVehicle computeVehicle(String type, SpatialRandom rand, int sid){
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
			return computeVehicle(type, rand, sid);
		}
		
		PointList route = path.getPoints();
		if(route == null || route.size() <= 1) {
			return computeVehicle(type, rand, sid);
		}
			
		Coordinate[] routeCoordinates = new Utils().toCoordinates(route);
		//LineString routeInStep = new Interpolate().routeInterpolateBySec(lsRoute, path.getTime()/1000, path.getDistance(), 1.0);
		if(routeCoordinates == null || routeCoordinates.length <= 1) {
			return computeVehicle(type, rand, sid);
		}
		
		IDMVehicle vehicle = new IDMVehicle(routeCoordinates, sid, path.getTime()/1000, path.getDistance());
		//vehicle.apply(src, dest, path.getDistance(), path.getTime()/1000);

		return vehicle;		
	}

}
