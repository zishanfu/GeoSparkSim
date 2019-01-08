package com.zishanfu.vistrips.osm;

import org.jxmapviewer.viewer.GeoPosition;
import org.openstreetmap.osmosis.xml.v0_6.XmlDownloader;

import com.zishanfu.vistrips.hdfs.HDFSUtil;

public class OsmParser {
	private static String tmpPath = "";
	
	public static String run(GeoPosition geo1, GeoPosition geo2) {
		String destPath= "/vistrips/rawOSM/";
		HDFSUtil.mkdir(destPath);
		HDFSUtil.deleteDir(tmpPath);
		tmpPath = HDFSUtil.mkdirTemp();
		String osmUrl = "http://overpass-api.de/api";
		XmlDownloader xmlDownloader = new XmlDownloader(geo1.getLongitude(), geo2.getLongitude(), geo1.getLatitude(), geo2.getLatitude(), osmUrl);
		xmlDownloader.setSink(new OsmParquetSink(tmpPath));
		xmlDownloader.run();
		return tmpPath;
	}
}
