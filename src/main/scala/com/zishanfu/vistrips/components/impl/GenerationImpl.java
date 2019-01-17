package com.zishanfu.vistrips.components.impl;

import org.apache.log4j.Logger;
import org.jxmapviewer.viewer.GeoPosition;
import com.zishanfu.vistrips.map.GraphInit;
import com.zishanfu.vistrips.map.OsmLoader;
import com.zishanfu.vistrips.map.TripsGeneration;
import com.zishanfu.vistrips.model.Pair;
import com.zishanfu.vistrips.tools.Distance;

public class GenerationImpl {
	private final Logger LOG = Logger.getLogger(GenerationImpl.class);
	private int tripLength = 0;
	
	public Pair[] apply(GeoPosition geo1, GeoPosition geo2, String selectedType, int total) {
		//scale the length of trip, same with the scale in trip generation
		double maxLen = new Distance().euclidean(geo1, geo2) / 10; 
		
		//[33.41281563419366, -111.94192886352539], [33.38816625881332, -111.88845634460449]
		//geo1.lat + maxLen, geo1.lon - maxLen
		//geo2.lat - maxLen, geo2.lon + maxLen
		GeoPosition newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen);
		GeoPosition newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen);
		//System.out.println(String.format("Selected rectangle, p1: %s, p2: %s", newGeo1, newGeo2));

		//String path = OsmParser.run(newGeo1, newGeo2);
		//textArea.append("Finished osm download and processing!\n");
		OsmLoader osmloader = new OsmLoader(newGeo1, newGeo2);
		LOG.warn("Downloading selected OSM data ...");
		
		String path = osmloader.download();
		String size = osmloader.getLastFileSize();
		LOG.warn(String.format("Finished download! Data %s located in ( %s )", size, path));
		LOG.warn("Processing graph ...");
		GraphInit gi = new GraphInit(osmloader.lastPath);
		//OsmGraph graph = new OsmGraph(spark, path);
		LOG.warn("Finished graph construction");

//		int nums = Integer.parseInt(numTxt.getText());
//		textArea.append("Begin generate " + nums +" trips\n");
//		TripsGeneration tg = new TripsGeneration(geo1, geo2, graph, maxLen);
//		Pair[] pairs = tg.computePairs(nums, selectedType);
//		sbHandler.setPairs(pairs);
//		
//    	long endTime = System.currentTimeMillis();
//    	textArea.append("Processed! Total time: " + (endTime - startTime)/1000 + " seconds\n");
		
		LOG.warn(String.format("Begin generate %s trips.", total));
		TripsGeneration tg = new TripsGeneration(geo1, geo2, gi, maxLen);
		Pair[] pairs = tg.computePairs(total, selectedType);
		this.tripLength = tg.getLongestTrip();
		LOG.warn(String.format("Finished! Longest trips time %s in second", tg.getLongestTripTime()));
		return pairs;
	}
	
	public int getTripLength() {
		return tripLength;
	}
}
