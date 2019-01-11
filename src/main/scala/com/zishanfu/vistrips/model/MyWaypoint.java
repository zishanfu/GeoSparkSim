package com.zishanfu.vistrips.model;

import java.awt.Color;
import java.io.Serializable;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class MyWaypoint extends DefaultWaypoint implements Serializable
{
    private final Color color;
    private int curIdx;
    private GeoPosition curPos;
    private LineString route;

    /**
     * @param color the color
     * @param route the route
     */
    public MyWaypoint(Color color, LineString route)
    {
        this.color = color;
        this.route = route;
        this.curIdx = 0;
    }
    

    public GeoPosition getCurPos() {
		return curPos;
	}
    
    
    public int getCurIdx() {
		return curIdx;
	}


	public int getRouteLength() {
    	return route.getNumPoints();
    }

	/**
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }
    
    
    public void update() {
    	if(curIdx == getRouteLength()) {
    		curPos = null;
    		return;
    	}
    	//lat, lon	
    	Coordinate coor = route.getCoordinateN(curIdx);
    	curPos = new GeoPosition(coor.y, coor.x);
    	curIdx++;
    }

}
