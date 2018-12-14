package com.zishanfu.vistrips.components;

import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.map.MapServiceImpl.GraphInit;
import com.zishanfu.vistrips.map.MapServiceImpl.OSMLoader;
import com.zishanfu.vistrips.map.MapServiceImpl.TripsGeneration;
import com.zishanfu.vistrips.model.Pair;
import com.zishanfu.vistrips.tools.Distance;


public class GenerateBtnHandler implements ActionListener{
	
	private SelectionAdapter sa;
	private SimulationBtnHandler sbHandler;
	private TextField numTxt;
	private JTextArea textArea;
	private String[] genTypes;
	private JComboBox genList;
	
	public GenerateBtnHandler(SelectionAdapter sa, TextField numTxt, SimulationBtnHandler sbHandler, 
			JTextArea textArea, String[] genTypes, JComboBox genList) {
		this.sa = sa;
		this.numTxt = numTxt;
		this.sbHandler = sbHandler;
		this.textArea = textArea;
		this.genTypes = genTypes;
		this.genList = genList;
	}

	public void actionPerformed(ActionEvent e) {
		if(sa.getViewer().getOverlayPainter() == null || numTxt.getText() == null || numTxt.getText().length() == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"You must select an area and enter the number of moving objects first!");
		
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
		    		double maxLen = new Distance().euclidean(geo1, geo2) / 5; //scale the length of trip
		    		
		    		//Download OSM
		    		OSMLoader osmloader = new OSMLoader(null, geo1, geo2);
		    		textArea.append("Downloading selected OSM data ...\n");
		    		String path = osmloader.download();
		    		String size = osmloader.getLastFileSize();
		    		textArea.append("Finished download! Data " + size + " located in (" + path + ")\n");
		    		
		    		//Processing Graph 
		    		textArea.append("Processing graph");
		    		GraphInit gi = new GraphInit(osmloader.lastPath);
		    		
		    		
		    		//Generating Trips
		    		int nums = Integer.parseInt(numTxt.getText());
		    		textArea.append("Begin generate " + nums +" trips");
		    		TripsGeneration tg = new TripsGeneration(geo1, geo2, gi, maxLen);
		    		Pair[] pairs = tg.computePairs(nums, selectedType);
		    		sbHandler.setPairs(pairs);
		    		
		        	long endTime = System.currentTimeMillis();
		        	textArea.append("Processed! Total time: " + (endTime - startTime)/1000 + " seconds\n");
		        }     
		    });
			
		    t.start();
			
		}
	}

}
