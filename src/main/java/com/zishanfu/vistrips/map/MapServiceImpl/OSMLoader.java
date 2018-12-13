package com.zishanfu.vistrips.map.MapServiceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.map.OSM;

/**
 * @author zishanfu
 *
 */
public class OSMLoader implements OSM{
	
	//"http://overpass-api.de/api/map?bbox=-111.93000,33.41237,-111.92175,33.41732";
	private String OSM_URL = "http://overpass-api.de/api/map?bbox=";
	private String fileBase = System.getProperty("user.home");
	private URL url;
	public String lastPath;
	
	/**
	 * OSMLoader downloads OSM file by provided bbx
	 * Example, geo1: -111.93000,33.41237, geo2: -111.92175,33.41732"
	 * @param path location to save file
	 * @param geo1 
	 * @param geo2
	 * @throws MalformedURLException 
	 */
	public OSMLoader(String path, GeoPosition geo1, GeoPosition geo2) {
		if(geo1.getLongitude() < geo2.getLongitude()) {
			GeoPosition tmp = geo1;
			geo1 = geo2;
			geo2 = tmp;
		}
		//"-111.93000,33.41237,-111.92175,33.41732"
		this.OSM_URL += geo1.getLongitude() + "," + geo1.getLatitude() + "," + 
						geo2.getLongitude() + "," + geo2.getLatitude();
		if(path == null || path.length() == 0) {
			path = "/Downloads/datasets/vistrips/osm_temp/";
		}
		this.fileBase += path;
		this.lastPath = fileBase;
		try {
			this.url = new URL(OSM_URL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @return the path of OSM file
	 * @throws IOException
	 */
	public String download() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String time = dateFormat.format(now);
		
		try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(fileBase+time);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            lastPath = fileBase+time;
            return fileBase+time;
        } catch (IOException e) {
        	e.printStackTrace();
        	return null;
        }
	}


	public String getLastFileSize() {
		File osm = new File(lastPath);
		return osm.length() / (1024 * 1024) + "MB";
	}


	
}
