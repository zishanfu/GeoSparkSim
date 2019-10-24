package com.zishanfu.geosparksim.trafficUI.model;

import java.awt.*;

public class GeoPoint extends Point {
    private Color color = Color.black;

    public GeoPoint(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public GeoPoint(double latitude, double longitude, Color color) {
        super(latitude, longitude);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public GeoPoint(Point point) {
        this(point.getLatitude(), point.getLongitude());
    }
}
