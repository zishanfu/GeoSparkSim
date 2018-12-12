package com.zishanfu.vistrips.components;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.jxmapviewer.JXMapViewer;

public class SelectionAdapter extends MouseAdapter 
{
    private JXMapViewer viewer;
    private int SIZE = 8;
    private Rectangle2D[] points = { new Rectangle2D.Double(100, 100,SIZE, SIZE), new Rectangle2D.Double(700, 400,SIZE, SIZE) };
    
    Rectangle2D r = new Rectangle2D.Double(0,0,SIZE,SIZE);
    private int pos = -1;

    /**
     * @param viewer the jxmapviewer
     */
    public SelectionAdapter(JXMapViewer viewer)
    {
        this.viewer = viewer;
    }
    
    public Rectangle2D[] getPoints() {
		return points;
	}
    
    
    public JXMapViewer getViewer() {
		return viewer;
	}


	public void mousePressed(MouseEvent event) {
    	Point p = event.getPoint();

    	for (int i = 0; i < points.length; i++) {
    		if (points[i].contains(p)) {
    			pos = i;
    			return;
    		}
    	}
    }

    public void mouseReleased(MouseEvent event) {
      pos = -1;
    }
    

    public void mouseDragged(MouseEvent event) {
    	if (pos == -1) {
    		return;
    	}
    	
    	if(points[1].getX() < points[0].getX()) {
    		return;
    	}

    	points[pos].setRect(event.getPoint().x,event.getPoint().y,points[pos].getWidth(),
    			points[pos].getHeight());

    	viewer.repaint();
    }
    

}
