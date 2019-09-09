package com.zishanfu.geosparksim.trafficUI;

import com.zishanfu.geosparksim.trafficUI.model.GeoPoint;
import com.zishanfu.geosparksim.trafficUI.model.Segment;
import com.zishanfu.geosparksim.trafficUI.model.Point;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@SuppressWarnings("serial")
class MapPanel extends JPanel {
    private final List<Segment> segments = new ArrayList<>();
    private final List<List<Segment>> vehicles = new ArrayList<>();
    private final List<List<GeoPoint>> signals = new ArrayList<>();
    private double minEasting, maxEasting, minNorthing, maxNorthing;
    private double oEasting, oNorthing;		// coordinates of the origin
    private double scale = -1;
    private int idx = 0;

    public MapPanel() {
        setMinimumSize(new Dimension(400, 300));
        setPreferredSize(new Dimension(800, 600));

        resetMinMaxEastingNorthing();

        addMouseWheelListener(new MouseWheelZoomer());

        MousePanner mousePanner = new MousePanner();
        addMouseListener(mousePanner);
        addMouseMotionListener(mousePanner);
    }


    public void run(){
        Thread animationThread = new Thread(new Runnable() {
            public void run() {
                while (idx < vehicles.size() - 1) {
                    idx++;
                    repaint();
                    try {Thread.sleep(500);} catch (Exception ex) {}
                }
            }
        });

        animationThread.start();
    }

    @Override
    protected synchronized void paintComponent(Graphics g_) {
        super.paintComponent(g_);

        Graphics2D g = (Graphics2D) g_;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        if(segments.size() == 0) return;
        if(this.scale == -1) scale();

        for(Segment seg : segments) {
            Point pA = seg.getPointA();
            Point pB = seg.getPointB();

            g.setColor(seg.getColor());

            int pA_x = convertX(pA.getEasting());
            int pA_y = convertY(pA.getNorthing(), h);
            int pB_x = convertX(pB.getEasting());
            int pB_y = convertY(pB.getNorthing(), h);
            g.drawLine(pA_x, pA_y, pB_x, pB_y);
        }

        if(signals.size() == 0) return;
        for (GeoPoint s: signals.get(idx)){
            g.setColor(s.getColor());
            int x = convertX(s.getEasting());
            int y = convertY(s.getNorthing(), h);
            g.fillOval(x-1, y-1, 10, 10);
        }

        if(vehicles.size() == 0) return;
        for (Segment v: vehicles.get(idx)){
            Point pA = v.getPointA();
            Point pB = v.getPointB();

            g.setStroke(v.getStroke());
            g.setColor(v.getColor());

            int pA_x = convertX(pA.getEasting());
            int pA_y = convertY(pA.getNorthing(), h);
            int pB_x = convertX(pB.getEasting());
            int pB_y = convertY(pB.getNorthing(), h);
            g.drawLine(pA_x, pA_y, pB_x, pB_y);
        }

        g.setColor(Color.BLACK);

        // unit is the unit of the scale. It must be a power of ten, such that unit * scale in [25, 250]
        double unit = Math.pow(10, Math.ceil(Math.log10(25/scale)));
        String strUnit;
        if(unit >= 1) strUnit = ((int) unit) + " km";
        else strUnit = ((int) (1000*unit)) + " m";
        g.drawString(strUnit + " \u2194 " + ((int)(unit * scale)) + " px", 10, 10+g.getFontMetrics().getHeight());
        // draw a 1-kilometer segment
        for(int i=6; i<=9; i++) {
            g.drawLine(10, i, 10+(int)(unit*scale*(i<8 ? 1 : .5)), i);
        }
    }

    public synchronized void clear() {
        this.segments.clear();
        resetMinMaxEastingNorthing();
    }

    public synchronized void addSegment(Segment segment) {
        this.segments.add(segment);

        updateMinMaxEastingNorthing(segment.getPointA());
        updateMinMaxEastingNorthing(segment.getPointB());
    }

    public void addSegments(Collection<Segment> segments) {
        for(Segment seg : segments) addSegment(seg);
    }

    public void addSignal(GeoPoint signal, int step) {
        while (this.signals.size() <= step){
            this.signals.add(new ArrayList<>());
        }
        this.signals.get(step).add(signal);
    }

    public void addVehicle(Segment vehicle, int step){
        while (this.vehicles.size() <= step){
            this.vehicles.add(new ArrayList<>());
        }
        this.vehicles.get(step).add(vehicle);
    }

    private synchronized void updateMinMaxEastingNorthing(Point point) {
        double easting = point.getEasting();
        if(easting > maxEasting) maxEasting = easting;
        if(easting < minEasting) minEasting = easting;

        double northing = point.getNorthing();
        if(northing > maxNorthing) maxNorthing = northing;
        if(northing < minNorthing) minNorthing = northing;
    }

    private synchronized void resetMinMaxEastingNorthing() {
        minEasting = Double.MAX_VALUE;
        maxEasting = Double.MIN_VALUE;
        minNorthing = Double.MAX_VALUE;
        maxNorthing = Double.MIN_VALUE;

        this.scale = -1;
    }

    private synchronized void scale() {
        int w = getWidth();
        int h = getHeight();

        this.scale = Math.min(
                w / (maxEasting - minEasting),
                h / (maxNorthing - minNorthing));

        oEasting = minEasting;
        oNorthing = minNorthing;
    }

    private int applyScale(double km) {
        return (int)(km*scale);
    }

    private int convertX(double easting) {
        return applyScale(easting - oEasting);
    }

    private int convertY(double northing, int height) {
        return height - applyScale(northing - oNorthing);
    }

    class MouseWheelZoomer implements MouseWheelListener {
        private static final double zoomFactor = .05;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double oldScale = scale;

            int rotation = e.getWheelRotation();
            if(rotation > 0) {
                scale /= (1 + rotation * zoomFactor);
            } else {
                scale *= (1 - rotation * zoomFactor);
            }

            // When zooming, the easting/northing at the cursor position must
            // remain the same, so we have to pan in addition to changing the
            // scale. The maths for easting (same goes for northing):
            //
            // before: x = (easting - oEasting) * oldScale
            // after: x = (easting - newOEasting) * scale
            //
            // (x remains the same, easting remains the same)
            //
            // hence: newOEasting = easting - (easting - oEasting) * oldScale / scale
            // with: easting = x/scale + oEasting
            // hence finally: newOEasting = oEasting + x * (1/oldScale - 1/scale)
            int x = e.getX();
            int y = e.getY();
            int h = getHeight();

            oEasting = oEasting + x * (1/oldScale - 1/scale);
            oNorthing = oNorthing + (h - y) * (1/oldScale - 1/scale);

            repaint();
        }
    }

    private class MousePanner implements MouseListener, MouseMotionListener {
        private int dragOriginX, dragOriginY;
        private double dragOriginOEasting, dragOriginONorthing;

        @Override
        public void mousePressed(MouseEvent e) {
            dragOriginX = e.getX();
            dragOriginY = e.getY();
            dragOriginOEasting = oEasting;
            dragOriginONorthing = oNorthing;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int deltaX = e.getX() - dragOriginX;
            int deltaY = e.getY() - dragOriginY;

            oEasting = dragOriginOEasting - deltaX / scale;
            oNorthing = dragOriginONorthing + deltaY / scale;

            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }
}