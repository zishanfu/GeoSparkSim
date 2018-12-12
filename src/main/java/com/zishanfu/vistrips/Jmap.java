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

public class Jmap {
	public static void main(String[] args) {
		 // Create a TileFactoryInfo for OpenStreetMap
		final JXMapViewer jXMapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

        // Setup JXMapViewer
        jXMapViewer.setTileFactory(tileFactory);

        GeoPosition tempe = new GeoPosition(33.388414, -111.931782); 

        // Set the focus
        jXMapViewer.setZoom(7);
        jXMapViewer.setAddressLocation(tempe);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(jXMapViewer);
        jXMapViewer.addMouseListener(mia);
        jXMapViewer.addMouseMotionListener(mia);
        jXMapViewer.addMouseListener(new CenterMapListener(jXMapViewer));
        jXMapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(jXMapViewer));
        jXMapViewer.addKeyListener(new PanKeyListener(jXMapViewer));

        // Add a selection painter
        final SelectionAdapter sa = new SelectionAdapter(jXMapViewer);
        final SelectionPainter sp = new SelectionPainter(sa);
       
        jXMapViewer.addMouseListener(sa);
        jXMapViewer.addMouseMotionListener(sa);
        
        
        //Bottom Panel
        JTextArea textArea = new JTextArea();
        textArea.setRows(3);
        textArea.setText("loading....");
        JScrollPane bottomPanel = new JScrollPane(textArea);

        
        //Top panel
        JPanel topPanel = new JPanel();
        JLabel label = new JLabel("VisTrips");
        Checkbox chkArea = new Checkbox("Select an area and enter the number of moving objects");
        chkArea.setFocusable(false);
        TextField num = new TextField(10);
        JButton gBtn = new JButton("Generate Scenario");
        JButton sBtn = new JButton("Begin Simulation");
   
        SimulationBtnHandler sbHandler = new SimulationBtnHandler(jXMapViewer);
        gBtn.addActionListener(new GenerateBtnHandler(sa, num, sbHandler, textArea));
        sBtn.addActionListener(sbHandler);

        topPanel.setLayout(new FlowLayout());
        topPanel.add(label);
        topPanel.add(chkArea);
        topPanel.add(num);
        topPanel.add(gBtn);
        topPanel.add(sBtn);

        

        // Display the viewer in a JFrame
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(jXMapViewer, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        chkArea.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		// TODO Auto-generated method stub
        		if(e.getStateChange()==1) {
        			jXMapViewer.setOverlayPainter(sp);
        		}else {
        			jXMapViewer.setOverlayPainter(null);
        		}
        	}
        });
        
        

        jXMapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                updateWindowTitle(frame, jXMapViewer);
            }
        });

        jXMapViewer.addPropertyChangeListener("center", new PropertyChangeListener()
        {
            @Override
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
