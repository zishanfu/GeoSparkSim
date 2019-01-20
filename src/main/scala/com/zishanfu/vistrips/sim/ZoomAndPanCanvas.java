package com.zishanfu.vistrips.sim;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import com.zishanfu.vistrips.sim.model.Lane;


public class ZoomAndPanCanvas extends Canvas {

    private boolean init = true;

    private Point[] points;
    private Lane[] lanes;

    private ZoomAndPanListener zoomAndPanListener;

    public ZoomAndPanCanvas(Lane[] lanes) {
        this.zoomAndPanListener = new ZoomAndPanListener(this);
        this.addMouseListener(zoomAndPanListener);
        this.addMouseMotionListener(zoomAndPanListener);
        this.addMouseWheelListener(zoomAndPanListener);
        this.lanes = lanes;
    }

    public ZoomAndPanCanvas(int minZoomLevel, int maxZoomLevel, double zoomMultiplicationFactor) {
        this.zoomAndPanListener = new ZoomAndPanListener(this, minZoomLevel, maxZoomLevel, zoomMultiplicationFactor);
        this.addMouseListener(zoomAndPanListener);
        this.addMouseMotionListener(zoomAndPanListener);
        this.addMouseWheelListener(zoomAndPanListener);
    }

    public Dimension getPreferredSize() {
        return new Dimension(600, 500);
    }

    public void paint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        if (init) {
            // Initialize the viewport by moving the origin to the center of the window,
            // and inverting the y-axis to point upwards.
            init = false;
            Dimension d = getSize();
            int xc = d.width / 2;
            int yc = d.height / 2;
            g.translate(xc, yc);
            g.scale(1, -1);
            // Save the viewport to be updated by the ZoomAndPanListener
            zoomAndPanListener.setCoordTransform(g.getTransform());
        } else {
            // Restore the viewport after it was updated by the ZoomAndPanListener
            g.setTransform(zoomAndPanListener.getCoordTransform());
        }

        // Draw the axes
        for(Lane lane: lanes) {
        	paintLine(g, lane.getHead().x, lane.getHead().y, 
        			lane.getTail().x, lane.getTail().y);
        }
        
        // Create an "upside-down" font to correct for the inverted y-axis
        Font font = g.getFont();
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(1, -1);
        g.setFont(font.deriveFont(affineTransform));
        // Draw the points and their coordinates
        for (int i = 0; i < points.length; i++) {
            Point p = points[i];
//            g.drawLine((int)p.getX() - 5, (int)p.getY(), (int)p.getX() + 5, (int)p.getY());
//            g.drawLine((int)p.getX(), (int)p.getY() -5, (int)p.getX(), (int)p.getY() + 5);
//            g.drawString("P"+ i + "(" + p.getX() + "," + p.getY() + ")", (float) p.getX(), (float) p.getY());
            paintPoint(g, (int)p.getX(), (int)p.getY(), 5);
        }
     }
    
    
    protected void paintLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
    	g2d.drawLine(x1, y1, x2, y2);
    }
    
    protected void paintPoint(Graphics2D g2d, int x, int y, int size) {
    	g2d.setColor(Color.DARK_GRAY);
    	g2d.fillOval((int)x, (int)y, size, size);
    }
    

}
