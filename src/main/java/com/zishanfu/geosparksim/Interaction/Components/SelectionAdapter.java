package com.zishanfu.geosparksim.Interaction.Components;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import org.jxmapviewer.JXMapViewer;

public class SelectionAdapter extends MouseAdapter
{
    private JXMapViewer viewer;
    private int SIZE = 10;
    private Rectangle2D[] points = new Rectangle2D[2];

    private int pos = -1;

    /**
     * @param viewer the jxmapviewer
     */
    public SelectionAdapter(JXMapViewer viewer, int width, int height)
    {
        this.viewer = viewer;
        points[0] = new Rectangle2D.Double(width/6, height/8,SIZE, SIZE);
        points[1] = new Rectangle2D.Double(width/1.5, height/1.3,SIZE, SIZE);
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
