package com.zishanfu.vistrips.components;

import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.components.impl.GenerationImpl;
import com.zishanfu.vistrips.osm.OsmGraph;
import com.zishanfu.vistrips.sim.model.IDMVehicle;
import com.zishanfu.vistrips.tools.Distance;


public class GenerateBtnHandler implements ActionListener{
	private final Logger LOG = Logger.getLogger(GenerateBtnHandler.class);
	private SelectionAdapter sa;
	private SimulationBtnHandler sbHandler;
	private TextField numTxt;
	private JTextArea textArea;
	private String[] genTypes;
	private JComboBox genList;
	private SparkSession spark;
	
	public GenerateBtnHandler(SelectionAdapter sa, TextField numTxt, SimulationBtnHandler sbHandler, 
			JTextArea textArea, String[] genTypes, JComboBox genList, SparkSession spark) {
		this.sa = sa;
		this.numTxt = numTxt;
		this.sbHandler = sbHandler;
		this.textArea = textArea;
		this.genTypes = genTypes;
		this.genList = genList;
		this.spark = spark;
	}

	public void actionPerformed(ActionEvent e) {
		if(sa.getViewer().getOverlayPainter() == null || 
				numTxt.getText() == null || numTxt.getText().length() == 0 ) {
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
//		    		System.out.println(String.format("pt1 %s, pt2 %s, geo1 %s, geo2 %s, zoom %s", pt1, pt2, geo1, geo2, sa.getViewer().getZoom()));

		    		//int zoom = sa.getViewer().getZoom();
		        	
		    		String selectedType = genTypes[genList.getSelectedIndex()];
		    		int total = Integer.parseInt(numTxt.getText());
		    		
		    		double maxLen = new Distance().euclidean(geo1, geo2) / 10; 
		    		LOG.warn(String.format("Selected rectangle, p1: %s, p2: %s", geo1, geo2));
		    		
//		    		GenerationImpl gImpl = new GenerationImpl(spark);
//		    		
//		    		JavaRDD<IDMVehicle> vehicles = gImpl.apply(geo1, geo2, selectedType, total);
//		    		OsmGraph graph = new OsmGraph(spark, gImpl.getHdfs());
//		    		
//		    		sbHandler.setVehicles(vehicles);
//		    		sbHandler.setGraph(graph);

		    		long endTime = System.currentTimeMillis();
		        	textArea.append("Processed! Total time: " + (endTime - startTime)/1000 + " seconds\n");
		        }     
		    });
			
		    t.start();
			
		}
	}

}
