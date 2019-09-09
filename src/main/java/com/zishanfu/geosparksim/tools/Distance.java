package com.zishanfu.geosparksim.tools;

import com.vividsolutions.jts.geom.Coordinate;

public class Distance{

    private double euclideanCompute(double lat1, double lon1, double lat2, double lon2) {
        double deltaX = Math.abs(lat1 - lat2);
        double deltaY = Math.abs(lon1 - lon2);
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    /**
     * Calculates the distance between two points by haversine formula
     *
     * @param latitude1  Latitude of first coordinate
     * @param longitude1 Longitude of first coordinate
     * @param latitude2  Latitude of second coordinate
     * @param longitude2 Longitude of second coordinate
     * @return           GeoSparkSim.Distance between the two points, in meter
     */
    public double haversine(double latitude1, double longitude1, double latitude2, double longitude2) {
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude1 = Math.toRadians(longitude1);
        longitude2 = Math.toRadians(longitude2);

        double RADIUS_OF_EARTH = 6371;
        return (1000 * 2 * RADIUS_OF_EARTH * Math.asin(Math.sqrt(
                Math.sin((latitude2 - latitude1)/2) * Math.sin((latitude2 - latitude1)/2) +
                        Math.cos(latitude1) * Math.cos(latitude2) *
                                Math.sin((longitude2 - longitude1)/2) * Math.sin((longitude2 - longitude1) / 2))));
    }

    /**
     * Calculates the distance between two points by haversine formula
     *
     * @param coor1 Coordinate 1
     * @param coor2 Coordinate 2
     * @return Distance in meter
     */
    public double haversine(Coordinate coor1, Coordinate coor2) {
        return haversine(coor1.x, coor1.y, coor2.x, coor2.y);
    }

    /**
     * Calculates the area between two coordinates
     *
     * @param lat1  Latitude of first coordinate
     * @param lon1 Longitude of first coordinate
     * @param lat2  Latitude of second coordinate
     * @param lon2 Longitude of second coordinate
     * @return the area of two coordinates
     */
    public double rectArea(double lat1, double lon1, double lat2, double lon2) {
        double length = haversine(new Coordinate(lat1, lon2), new Coordinate(lat1, lon1));
        double height = haversine(new Coordinate(lat2, lon1), new Coordinate(lat2, lon2));
        return(length * height);
    }


    /**
     * Calculates the distance between two points by euclidean distance
     *
     * @param A, a node of coordinate
     * @param B, a node of coordinate
     * @return the distance between A and B
     */
    public double euclidean(double[] A, double[] B) {
        return euclideanCompute(A[0], A[1], B[0], B[1]);
    }


    /**
     * Calculates the distance between two points by euclidean distance
     *
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return
     */
    public double euclidean(double lat1, double lat2, double lon1, double lon2) {
        return euclideanCompute(lat1, lon1, lat2, lon2);
    }

}

