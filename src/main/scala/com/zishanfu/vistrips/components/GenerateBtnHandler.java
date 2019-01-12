package com.zishanfu.vistrips.components;

import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.util.PointList;
import com.zishanfu.vistrips.map.GraphInit;
import com.zishanfu.vistrips.map.OsmGraph;
import com.zishanfu.vistrips.map.OsmLoader;
import com.zishanfu.vistrips.map.TripsGeneration;
import com.zishanfu.vistrips.model.Pair;
import com.zishanfu.vistrips.tools.Distance;


public class GenerateBtnHandler implements ActionListener{
	private final Logger LOG = LoggerFactory.getLogger(GenerateBtnHandler.class);
	private SelectionAdapter sa;
	private SimulationBtnHandler sbHandler;
	private TextField numTxt;
	private JTextArea textArea;
	private TextField delayTxt;
	private String[] genTypes;
	private JComboBox genList;
	private SparkSession spark;
	
	public GenerateBtnHandler(SelectionAdapter sa, TextField numTxt, TextField delayTxt, SimulationBtnHandler sbHandler, 
			JTextArea textArea, String[] genTypes, JComboBox genList, SparkSession spark) {
		this.sa = sa;
		this.numTxt = numTxt;
		this.sbHandler = sbHandler;
		this.delayTxt = delayTxt;
		this.textArea = textArea;
		this.genTypes = genTypes;
		this.genList = genList;
		this.spark = spark;
	}

	public void actionPerformed(ActionEvent e) {
		if(sa.getViewer().getOverlayPainter() == null || 
				numTxt.getText() == null || numTxt.getText().length() == 0 ||
				delayTxt.getText() == null || delayTxt.getText().length() == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"You must select an area, enter the number of moving objects and delay time first!");
		
		}else {
			Thread t = new Thread(new Runnable() {
		        public void run() {
		        	textArea.append("Processing\n");
		        	long startTime = System.currentTimeMillis();
		        	
		            //Get bounds coordinates, selected type
		        	final Rectangle2D[] bounds = sa.getPoints();
		    		
		    		Point pt1 = new Point();
		    		pt1.setLocation(bounds[0].getX(), bounds[0].getY());
		    		
		    		Point pt2 = new Point();
		    		pt2.setLocation(bounds[1].getX(), bounds[1].getY());
		    		
		    		GeoPosition geo1 = sa.getViewer().convertPointToGeoPosition(pt1);
		    		GeoPosition geo2 = sa.getViewer().convertPointToGeoPosition(pt2);
		    		//int zoom = sa.getViewer().getZoom();
		        	
		    		String selectedType = genTypes[genList.getSelectedIndex()];
		    		//scale the length of trip, same with the scale in trip generation
		    		double maxLen = new Distance().euclidean(geo1, geo2) / 10; 
		    		System.out.println(String.format("Selected rectangle, p1: %s, p2: %s", geo1, geo2));
		    		
		    		//[33.41281563419366, -111.94192886352539], [33.38816625881332, -111.88845634460449]
		    		//geo1.lat + maxLen, geo1.lon - maxLen
		    		//geo2.lat - maxLen, geo2.lon + maxLen
		    		GeoPosition newGeo1 = new GeoPosition(geo1.getLatitude() + maxLen, geo1.getLongitude() - maxLen);
		    		GeoPosition newGeo2 = new GeoPosition(geo2.getLatitude() - maxLen, geo2.getLongitude() + maxLen);
		    		//System.out.println(String.format("Selected rectangle, p1: %s, p2: %s", newGeo1, newGeo2));

		    		//String path = OsmParser.run(newGeo1, newGeo2);
		    		//textArea.append("Finished osm download and processing!\n");
		    		OsmLoader osmloader = new OsmLoader(newGeo1, newGeo2);
		    		textArea.append("Downloading selected OSM data ...\n");
		    		String path = osmloader.download();
		    		String size = osmloader.getLastFileSize();
		    		textArea.append("Finished download! Data " + size + " located in (" + path + ")\n");
		    		
		    		textArea.append("Processing graph\n");
		    		GraphInit gi = new GraphInit(osmloader.lastPath);
		    		//OsmGraph graph = new OsmGraph(spark, path);
		    		textArea.append("Finished graph construction\n");
		    		

//		    		int nums = Integer.parseInt(numTxt.getText());
//		    		textArea.append("Begin generate " + nums +" trips\n");
//		    		TripsGeneration tg = new TripsGeneration(geo1, geo2, graph, maxLen);
//		    		Pair[] pairs = tg.computePairs(nums, selectedType);
//		    		sbHandler.setPairs(pairs);
//		    		
//		        	long endTime = System.currentTimeMillis();
//		        	textArea.append("Processed! Total time: " + (endTime - startTime)/1000 + " seconds\n");
		    		int nums = Integer.parseInt(numTxt.getText());
		    		
		    		textArea.append("Begin generate " + nums +" trips \n");
		    		TripsGeneration tg = new TripsGeneration(geo1, geo2, gi, maxLen);
		    		Pair[] pairs = tg.computePairs(nums, selectedType);
		    		
		    		sbHandler.setDelayInSec(Double.parseDouble(delayTxt.getText()));
		    		sbHandler.setPairs(pairs);

		        	long endTime = System.currentTimeMillis();
		        	textArea.append("Processed! Total time: " + (endTime - startTime)/1000 + " seconds\n");
		        	textArea.append("Real time " + tg.getLongestTripTime() +" in seconds \n");
		        }     
		    });
			
		    t.start();
			
		}
	}

}
