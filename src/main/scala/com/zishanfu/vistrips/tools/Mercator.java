package com.zishanfu.vistrips.tools;

import java.awt.Point;

import com.vividsolutions.jts.geom.Coordinate;

public final class Mercator
{
    private final int TILE_SIZE = 256;
    private Point _pixelOrigin;
    private double _pixelsPerLonDegree;
    private double _pixelsPerLonRadian;

    public Mercator()
    {
        this._pixelOrigin = new Point((int)(TILE_SIZE / 2.0),(int)(TILE_SIZE / 2.0));
        this._pixelsPerLonDegree = TILE_SIZE / 360.0;
        this._pixelsPerLonRadian = TILE_SIZE / (2 * Math.PI);
    }

    double bound(double val, double valMin, double valMax)
    {
        double res;
        res = Math.max(val, valMin);
        res = Math.min(res, valMax);
        return res;
    }

    double degreesToRadians(double deg) 
    {
        return deg * (Math.PI / 180);
    }

    double radiansToDegrees(double rad) 
    {
        return rad / (Math.PI / 180);
    }

    Point fromLatLngToPoint(double lat, double lng, int zoom)
    {
        Point point = new Point(0, 0);

        point.x = (int) (_pixelOrigin.x + lng * _pixelsPerLonDegree);       

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        double siny = bound(Math.sin(degreesToRadians(lat)), -0.9999,0.9999);
        point.y = (int) (_pixelOrigin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) *- _pixelsPerLonRadian);

        int numTiles = 1 << zoom;
        point.x = point.x * numTiles;
        point.y = point.y * numTiles;
        return point;
     }

    Coordinate fromPointToLatLng(Point point, int zoom)
    {
        int numTiles = 1 << zoom;
        point.x = point.x / numTiles;
        point.y = point.y / numTiles;       

        double lng = (point.x - _pixelOrigin.x) / _pixelsPerLonDegree;
        double latRadians = (point.y - _pixelOrigin.y) / - _pixelsPerLonRadian;
        double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
        return new Coordinate(lng, lat);
    }

    public static void main(String []args) 
    {
        Mercator gmap2 = new Mercator();
        Coordinate coor = new Coordinate(-111.89043045043945, 33.39024452836255);
        Point point1 = gmap2.fromLatLngToPoint(41.850033, -87.6500523, 3);
        System.out.println(point1.x+"   "+point1.y);
        Coordinate point2 = gmap2.fromPointToLatLng(point1,3);
        System.out.println(point2.x+"   "+point2.y);
        
        SphericalMercator sm = new SphericalMercator();
        Point point3 = sm.coordinate2Point(coor, 3);
        System.out.println(point3.x+"   "+point3.y);
        Coordinate point4 = sm.point2Coordinate(point3, 3);
        System.out.println(point4.x+"   "+point4.y);
        
    }
}

