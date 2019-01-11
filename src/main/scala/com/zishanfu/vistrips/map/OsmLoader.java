package com.zishanfu.vistrips.map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmLoader{
	
	private final Logger LOG = LoggerFactory.getLogger(OsmLoader.class);
	//"http://overpass-api.de/api/map?bbox=-111.93000,33.41237,-111.92175,33.41732";
	private String OSM_URL = "http://overpass-api.de/api/map?bbox=";
	private String pathBase = System.getProperty("user.dir") + "/src/test/resources/vistrips";
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
	public OsmLoader(GeoPosition geo1, GeoPosition geo2) {
		double left = Math.min(geo1.getLongitude(), geo2.getLongitude());
		double right = Math.max(geo1.getLongitude(), geo2.getLongitude());
		double top = Math.max(geo1.getLatitude(), geo2.getLatitude());
		double bottom = Math.min(geo1.getLatitude(), geo2.getLatitude());
		//"-111.93000,33.41237,-111.92175,33.41732"
		this.OSM_URL += left + "," + bottom + "," + 
						right + "," + top;
		
		directoryOps(pathBase);

		try {
			this.url = new URL(OSM_URL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void directoryOps(String directory) {
		try {
            File f = new File(directory);
            if(f.isDirectory()) {
            	FileUtils.cleanDirectory(f); 
                FileUtils.forceDelete(f); 
            }
            FileUtils.forceMkdir(f); 
        } catch (IOException e) {
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
		String newFileName = pathBase + "/" + dateFormat.format(now) + ".osm";
		
		try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(newFileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            lastPath = newFileName;
            return newFileName;
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