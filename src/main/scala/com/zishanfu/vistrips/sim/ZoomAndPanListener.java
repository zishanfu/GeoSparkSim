package com.zishanfu.vistrips.sim;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;


public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    public static final int DEFAULT_MIN_ZOOM_LEVEL = -20;
    public static final int DEFAULT_MAX_ZOOM_LEVEL = 10;
    public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 1.2;

    private Component targetComponent;

    private int zoomLevel = 0;
    private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
    private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
    
    private Point dragStartScreen;
    private Point dragEndScreen;
    private AffineTransform coordTransform = new AffineTransform();

    public ZoomAndPanListener(Component targetComponent) {
        this.targetComponent = targetComponent;
    }

    public ZoomAndPanListener(Component targetComponent, int minZoomLevel, int maxZoomLevel, double zoomMultiplicationFactor) {
        this.targetComponent = targetComponent;
        this.minZoomLevel = minZoomLevel;
        this.maxZoomLevel = maxZoomLevel;
        this.zoomMultiplicationFactor = zoomMultiplicationFactor;
    }


    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        dragStartScreen = e.getPoint();
        dragEndScreen = null;
    }

    public void mouseReleased(MouseEvent e) {
//        moveCamera(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        moveCamera(e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
//        System.out.println("============= Zoom camera ============");
        zoomCamera(e);
    }

    private void moveCamera(MouseEvent e) {
//        System.out.println("============= Move camera ============");
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            targetComponent.repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private void zoomCamera(MouseWheelEvent e) {
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            if (wheelRotation > 0) {
                if (zoomLevel < maxZoomLevel) {
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    targetComponent.repaint();
                }
            } else {
                if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    targetComponent.repaint();
                }
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
//        System.out.println("Model -> Screen Transformation:");
//        showMatrix(coordTransform);
        AffineTransform inverse = coordTransform.createInverse();
//        System.out.println("Screen -> Model Transformation:");
//        showMatrix(inverse);
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    private void showMatrix(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);  // { m00 m10 m01 m11 m02 m12 }
        int[] loRow = {0, 0, 1};
        for (int i = 0; i < 2; i++) {
            System.out.print("[ ");
            for (int j = i; j < matrix.length; j += 2) {
                System.out.printf("%5.1f ", matrix[j]);
            }
            System.out.print("]\n");
        }
        System.out.print("[ ");
        for (int i = 0; i < loRow.length; i++) {
            System.out.printf("%3d   ", loRow);
        }
        System.out.print("]\n");
        System.out.println("---------------------");
     }

    public int getZoomLevel() {
    	return zoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
    	this.zoomLevel = zoomLevel;
    }
    
    public AffineTransform getCoordTransform() {
    	return coordTransform;
    }
    
    public void setCoordTransform(AffineTransform coordTransform) {
    	this.coordTransform = coordTransform;
    }

}

