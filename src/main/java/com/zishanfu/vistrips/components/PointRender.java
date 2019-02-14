package com.zishanfu.vistrips.components;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointRenderer;

import com.zishanfu.vistrips.model.MyWaypoint;


public class PointRender implements WaypointRenderer<MyWaypoint>{
	
	int width = 7;
	int height = 7;
	
    public PointRender(){}

    public void paintWaypoint(Graphics2D g, JXMapViewer viewer, MyWaypoint obj)
    {
    	if(obj == null) 
    		return;
    	
        g = (Graphics2D)g.create();
        GeoPosition pos = obj.getCurPos();
        
        if(pos == null )
        	return;
        
        Point2D point = viewer.getTileFactory().geoToPixel(pos, viewer.getZoom());

        int x = (int)point.getX();
        int y = (int)point.getY();
        
        g.setColor(obj.getColor());
        g.fillOval(x, y, width, height);

        g.dispose();

    }
     
    

}
