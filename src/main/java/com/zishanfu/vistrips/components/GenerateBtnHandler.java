package com.zishanfu.vistrips.components;

import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;


import javax.swing.JTextArea;

import org.jxmapviewer.viewer.GeoPosition;

import com.zishanfu.vistrips.map.OSMLoader;
import com.zishanfu.vistrips.map.TripsGeneration;
import com.zishanfu.vistrips.model.Pair;


public class GenerateBtnHandler implements ActionListener{
	
	private SelectionAdapter sa;
	private SimulationBtnHandler sbHandler;
	private TextField num;
	private JTextArea textArea;
	
	public GenerateBtnHandler(SelectionAdapter sa, TextField num, SimulationBtnHandler sbHandler, JTextArea textArea) {
		this.sa = sa;
		this.num = num;
		this.sbHandler = sbHandler;
		this.textArea = textArea;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(sa.getViewer().getOverlayPainter() == null || num.getText() == null || num.getText().length() == 0) {
			AttentionDialog dialog = new AttentionDialog("Attention", 
					"You must select an area and enter the number of moving objects first!");
		
		}else {
			long startTime = System.currentTimeMillis();
			final Rectangle2D[] bounds = sa.getPoints();
			
			Point pt1 = new Point();
			pt1.setLocation(bounds[0].getX(), bounds[0].getY());
			
			Point pt2 = new Point();
			pt2.setLocation(bounds[1].getX(), bounds[1].getY());

			
			//int zoom = sa.getViewer().getZoom();
			
			GeoPosition geo1 = sa.getViewer().convertPointToGeoPosition(pt1);
			GeoPosition geo2 = sa.getViewer().convertPointToGeoPosition(pt2);
			OSMLoader osmloader = new OSMLoader(null, geo1, geo2);
			
			int nums = Integer.parseInt(num.getText());

			TripsGeneration tg = new TripsGeneration(geo1, geo2);
			Pair[] pairs = tg.computePairs(nums, tg.NB);
			sbHandler.setPairs(pairs);
			
			long endTime = System.currentTimeMillis();
			
			textArea.setText("Finished! Trips generation took " + (endTime - startTime) + " milliseconds");
		}
		
	}

}
