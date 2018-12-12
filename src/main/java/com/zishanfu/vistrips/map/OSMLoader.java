package com.zishanfu.vistrips.map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jxmapviewer.viewer.GeoPosition;

public class OSMLoader {
	
	//"http://overpass-api.de/api/map?bbox=-111.93000,33.41237,-111.92175,33.41732";
	private String OSM_URL = "http://overpass-api.de/api/map?bbox=";
	private String fileBase = System.getProperty("user.dir");
	
	
	/**
	 * OSMLoader downloads OSM file by provided bbx
	 * @param path location to save file
	 * @param geo1 
	 * @param geo2
	 */
	public OSMLoader(String path, GeoPosition geo1, GeoPosition geo2) {
		if(geo1.getLongitude() > geo2.getLongitude()) {
			GeoPosition tmp = geo1;
			geo1 = geo2;
			geo2 = tmp;
		}
		//"-111.93000,33.41237,-111.92175,33.41732"
		this.OSM_URL += geo1.getLongitude() + "," + geo1.getLatitude() + "," + 
						geo2.getLongitude() + "," + geo2.getLatitude();
		if(path == null || path.length() == 0) {
			path = "Downloads/datasets/vistrips/osm_temp/";
		}
		this.fileBase += path;
	}
	
	
	/**
	 * @return the path of OSM file
	 * @throws IOException
	 */
	public String download() throws IOException {
		URL url = new URL(OSM_URL);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("HEAD");
		long removeFileSize = httpConnection.getContentLengthLong();
		System.out.println(removeFileSize);
	
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh mm ss");
		String time = dateFormat.format(now);
		
		try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(fileBase+time);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            	return fileBase+time;
        } catch (IOException e) {
        		return null;
        }
	}
}
