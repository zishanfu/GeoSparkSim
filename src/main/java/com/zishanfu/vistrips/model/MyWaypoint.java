package com.zishanfu.vistrips.model;

import java.awt.Color;
import java.util.List;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

public class MyWaypoint extends DefaultWaypoint
{
    private final Color color;
    private int curIdx;
    private GeoPosition curPos;
    private List<Double[]> route;

    /**
     * @param color the color
     * @param route the route
     */
    public MyWaypoint(Color color, List<Double[]> route)
    {
        this.color = color;
        this.route = route;
        this.curIdx = 0;
        //this.curPos = new GeoPosition(route.get(curIdx)[1], route.get(curIdx)[0]);
    }
    

    public GeoPosition getCurPos() {
		return curPos;
	}
    
    
    public int getCurIdx() {
		return curIdx;
	}


	public int getRouteLength() {
    	return route.size();
    }

	/**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }
    
    
    public void update() {
    	if(curIdx == route.size()) {
    		curPos = null;
    		return;
    	}
    		
    	curPos = new GeoPosition(route.get(curIdx)[1], route.get(curIdx)[0]);
    	curIdx++;
    }

}
