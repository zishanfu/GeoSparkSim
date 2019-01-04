package com.zishanfu.vistrips.osm;

import org.jxmapviewer.viewer.GeoPosition;
import org.openstreetmap.osmosis.xml.v0_6.XmlDownloader;

import com.zishanfu.vistrips.hdfs.HDFSUtil;

public class OsmParser {
	public static void run(GeoPosition geo1, GeoPosition geo2) {
		String destPath= "/vistrips/rawOSM/";
		HDFSUtil.mkdir(destPath);
		String tmpPath = HDFSUtil.mkdirTemp();
		String osmUrl = "http://overpass-api.de/api";
		XmlDownloader xmlDownloader = new XmlDownloader(geo1.getLongitude(), geo2.getLongitude(), geo1.getLatitude(), geo2.getLatitude(), osmUrl);
		xmlDownloader.setSink(new OsmParquetSink(tmpPath));
		xmlDownloader.run();
	}
}
