package com.zishanfu.vistrips.model;

import java.io.Serializable;

import org.jxmapviewer.viewer.GeoPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class NewWaypoint extends LineString implements Serializable{
	
    private int curIdx;
    private GeoPosition curPos;
    private Coordinate[] route;
    
	public NewWaypoint(Coordinate[] points, PrecisionModel precisionModel, int SRID) {
		super(points, precisionModel, SRID);
		this.curIdx = 0;
		this.route = points;
	}
	
    public GeoPosition getCurPos() {
		return curPos;
	}
    
    public Coordinate getCurPosByN(int n) {
    	if(n >= getRouteLength()) {
    		return null;
    	}
		return route[n];
	}
    
    
    public int getCurIdx() {
		return curIdx;
	}


	public int getRouteLength() {
    	return route.length;
    }
    
    public void update() {
    	if(curIdx == getRouteLength()) {
    		curPos = null;
    		return;
    	}
    	//lat, lon	
    	Coordinate coor = route[curIdx];
    	curPos = new GeoPosition(coor.y, coor.x);
    	curIdx++;
    }
}
