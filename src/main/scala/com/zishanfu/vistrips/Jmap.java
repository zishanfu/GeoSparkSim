package com.zishanfu.vistrips;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.JXMapViewer;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;

import com.zishanfu.vistrips.controller.CompController;
import com.zishanfu.vistrips.controller.InputController;
import com.zishanfu.vistrips.controller.ResultController;

public class Jmap {
	private final static Logger LOG = Logger.getLogger(Jmap.class);
	
	public void runUI(SparkSession spark) {
		int width = 1200;
		int height = 800;
		CompController cc = new CompController(width, height);
		final JXMapViewer jXMapViewer = cc.mapViewer;
		ResultController rc = new ResultController();
		InputController ic = new InputController(cc, rc, spark);
		System.setProperty("org.geotools.referencing.forceXY", "true");
		

        // Display the viewer in a JFrame
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(jXMapViewer, BorderLayout.CENTER);
        frame.add(ic.inputPanel, BorderLayout.NORTH);
        frame.add(rc.resultPanel, BorderLayout.SOUTH);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        LOG.info("Jmap Configuration");
        
        jXMapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateWindowTitle(frame, jXMapViewer);
            }
        });

        jXMapViewer.addPropertyChangeListener("center", new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateWindowTitle(frame, jXMapViewer);
            }
        });

        updateWindowTitle(frame, jXMapViewer);
    }

    protected void updateWindowTitle(JFrame frame, JXMapViewer jXMapViewer)
    {
        double lat = jXMapViewer.getCenterPosition().getLatitude();
        double lon = jXMapViewer.getCenterPosition().getLongitude();
        int zoom = jXMapViewer.getZoom();

        frame.setTitle(String.format("VisTrips version 1 (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
    }

}
