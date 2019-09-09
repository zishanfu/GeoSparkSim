package com.zishanfu.geosparksim.trafficUI;


import com.zishanfu.geosparksim.trafficUI.model.GeoPoint;
import com.zishanfu.geosparksim.trafficUI.model.Segment;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;


@SuppressWarnings("serial")
public class MapWindow extends JFrame {
    private final MapPanel map;

    /**
     * Creates a new window.
     */
    public MapWindow(String appTitle) {
        super(appTitle);
        map = new MapPanel();
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Deletes all the registered segments and POIs.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Adds a segment to the list of segments to display.
     * @param segment the segment to add
     */
    public void addSegment(Segment segment) {
        map.addSegment(segment);
    }

    /**
     * Adds a whole collection of segments to the list of segments to display
     * @param segments the collection of segments to add
     */
    public void addSegments(Collection<Segment> segments) {
        map.addSegments(segments);
    }

    public void addSignal(GeoPoint signal, int step){ map.addSignal(signal, step);}

    public void addVehicle(Segment vehicle, int step){
        map.addVehicle(vehicle, step);
    }

    public void runSimulation(){
        map.run();
    }

}

