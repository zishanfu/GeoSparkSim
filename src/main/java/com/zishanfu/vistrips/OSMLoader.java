package com.zishanfu.vistrips;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OSMLoader {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//http://overpass-api.de/api/map?bbox=-111.93000,33.41237,-111.92175,33.41732
		String OSM_URL = "http://overpass-api.de/api/map?bbox=-111.93000,33.41237,-111.92175,33.41732";
		String fileBase = "/home/zishanfu/Downloads/datasets/vistrips/osm_temp/";
		
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

        } catch (IOException e) {
            e.printStackTrace();
        }
		

	}


}
