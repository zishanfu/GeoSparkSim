package com.zishanfu.vistrips.sim.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import com.zishanfu.vistrips.sim.model.GeoPoint;
import com.zishanfu.vistrips.sim.model.Point;
import com.zishanfu.vistrips.sim.model.Segment;

@SuppressWarnings("serial")
class MapPanel extends JPanel {
	private final List<Segment> segments = new ArrayList<Segment>();
	private final List<GeoPoint> pois = new ArrayList<GeoPoint>();
	private double minEasting, maxEasting, minNorthing, maxNorthing;
	private double oEasting, oNorthing;		// coordinates of the origin
	private double scale = -1;
	
	public MapPanel() {
		setMinimumSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(800, 600));
		
		resetMinMaxEastingNorthing();
		
		addMouseWheelListener(new MouseWheelZoomer());
		
		MousePanner mousePanner = new MousePanner();
		addMouseListener(mousePanner);
		addMouseMotionListener(mousePanner);
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
			g.setStroke(seg.getStroke());
			
			g.drawLine(
					convertX(pA.getEasting()), convertY(pA.getNorthing(), h),
					convertX(pB.getEasting()), convertY(pB.getNorthing(), h));
		}
		
		g.setColor(Color.BLACK);
		
		for(GeoPoint poi : pois) {
			int x = convertX(poi.getEasting());
			int y = convertY(poi.getNorthing(), h);
			g.fillOval(x-1, y-1, 3, 3);
			g.drawString(poi.getLabel(), x, y);
		}
		
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
		this.pois.clear();
		
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
	
	public synchronized void addPOI(GeoPoint poi) {
		this.pois.add(poi);
		
		updateMinMaxEastingNorthing(poi);
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
			
			//System.out.println(rotation + " => " + scale);
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
