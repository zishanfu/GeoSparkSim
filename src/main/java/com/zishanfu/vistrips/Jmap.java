package com.zishanfu.vistrips;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import com.zishanfu.vistrips.components.GenerateBtnHandler;
import com.zishanfu.vistrips.components.SelectionPainter;
import com.zishanfu.vistrips.components.SelectionAdapter;
import com.zishanfu.vistrips.components.SimulationBtnHandler;
import com.zishanfu.vistrips.controller.ControllerImpl.CompController;
import com.zishanfu.vistrips.controller.ControllerImpl.InputController;
import com.zishanfu.vistrips.controller.ControllerImpl.ResultController;

public class Jmap {
	public static void main(String[] args) {
		int width = 1200;
		int height = 800;
		CompController cc = new CompController(width, height);
		final JXMapViewer jXMapViewer = cc.mapViewer;
		ResultController rc = new ResultController();
		InputController ic = new InputController(cc, rc);
		

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

    protected static void updateWindowTitle(JFrame frame, JXMapViewer jXMapViewer)
    {
        double lat = jXMapViewer.getCenterPosition().getLatitude();
        double lon = jXMapViewer.getCenterPosition().getLongitude();
        int zoom = jXMapViewer.getZoom();

        frame.setTitle(String.format("VisTrips version 1 (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
    }

}
