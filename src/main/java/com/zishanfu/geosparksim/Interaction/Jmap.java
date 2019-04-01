package com.zishanfu.geosparksim.Interaction;


import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

import com.zishanfu.geosparksim.Interaction.Controller.CompController;
import com.zishanfu.geosparksim.Interaction.Controller.InputController;
import com.zishanfu.geosparksim.Interaction.Controller.ResultController;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.JXMapViewer;


public class Jmap {
    private final static Logger LOG = Logger.getLogger(Jmap.class);
    private final String appTitle;

    public Jmap(String appTitle){
        this.appTitle = appTitle;
    }

    public void runUI(SparkSession spark) {
        int width = 1200;
        int height = 800;
        CompController cc = new CompController(width, height);
        final JXMapViewer jXMapViewer = cc.mapViewer;
        ResultController rc = new ResultController();
        InputController ic = new InputController(cc, rc, spark, appTitle);

        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(jXMapViewer, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(ic.inputPanel, BorderLayout.NORTH);
        rightPanel.add(rc.resultPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST);

        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

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

        frame.setTitle(String.format(appTitle + " (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
    }

}
