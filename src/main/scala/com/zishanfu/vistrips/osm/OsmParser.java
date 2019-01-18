package com.zishanfu.vistrips.osm;

import org.jxmapviewer.viewer.GeoPosition;
import org.openstreetmap.osmosis.xml.v0_6.XmlDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zishanfu.vistrips.tools.HDFSUtil;


public class OsmParser {
	private static String tmpPath = "";
	private static final Logger LOG = LoggerFactory.getLogger(OsmParser.class);
	
	public static String run(GeoPosition geo1, GeoPosition geo2) {
		String destPath= "/vistrips";
		HDFSUtil.deleteDir(destPath);
		HDFSUtil.mkdir(destPath);
		tmpPath = HDFSUtil.mkdirTemp(destPath);
		LOG.info(String.format("Created a folder in HDFS located in %s", tmpPath));
		
		String osmUrl = "http://overpass-api.de/api";
		XmlDownloader xmlDownloader = new XmlDownloader(geo1.getLongitude(), geo2.getLongitude(), geo1.getLatitude(), geo2.getLatitude(), osmUrl);
		xmlDownloader.setSink(new OsmParquetSink(tmpPath));
		xmlDownloader.run();
		LOG.info(String.format("Sinked openstreetmap data"));
		return tmpPath;
	}
}
