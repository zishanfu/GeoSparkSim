package com.zishanfu.geosparksim.Model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.zishanfu.geosparksim.model.SegmentNode;

import java.util.ArrayList;
import java.util.List;


public class Link extends LineString {
    private long id;
    private SegmentNode head;
    private SegmentNode tail;
    private double distance;
    private int speed;
    private int driveDirection;
    private int lanes;
    private double angle;
    private String laneArray;
    private Coordinate[][] laneMatrix;
    private Coordinate[][] laneCenterMatrix;
    private List<List<MOBILVehicle>> laneVehicles;
    private double minLat = Double.MAX_VALUE;
    private double maxLat = Double.MIN_VALUE;
    private double minLon = minLat;
    private double maxLon = maxLat;

    public Link(long id, SegmentNode head, SegmentNode tail, double distance, int speed, int driveDirection, int lanes, double angle, String laneArray, Coordinate[] path){
        super(path, new PrecisionModel(), 1);
        this.id = id;
        this.head = head;
        this.tail = tail;
        this.distance = distance;
        this.speed = speed;
        this.driveDirection = driveDirection;
        this.lanes = lanes;
        this.angle = angle;
        this.laneArray = laneArray;
    }

    public Link(Coordinate[] points, PrecisionModel precisionModel, int SRID) {
        super(points, precisionModel, SRID);
    }


    public long getId() {
        return id;
    }

    public SegmentNode getHead() {
        return head;
    }

    public SegmentNode getTail() {
        return tail;
    }

    public double getDistance() {
        return distance;
    }

    public int getSpeed() {
        return speed;
    }

    public int getDriveDirection() {
        return driveDirection;
    }

    public int getLanes() {
        return lanes;
    }

    public double getAngle() {
        return angle;
    }

    public String getLaneArray() {
        return laneArray;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLon() {
        return maxLon;
    }

    public List<List<MOBILVehicle>> getLaneVehicles() {
        return laneVehicles;
    }

    public void removeVehicleFromLane(MOBILVehicle vehicle, int lane){
        List<MOBILVehicle> list = new ArrayList<>();
        if (laneVehicles.get(lane).size() == 0){
            return;
        }

        for (MOBILVehicle veh: laneVehicles.get(lane)){
            if (veh.getId().equals(vehicle.getId())) continue;
            list.add(veh);
        }

        laneVehicles.set(lane, list);
    }

    public void addVehicleToLane(MOBILVehicle vehicle, int lane){
        List<MOBILVehicle> list = laneVehicles.get(lane);
        list.add(vehicle);
        laneVehicles.set(lane, list);
    }

    public void setLaneMatrix(Coordinate[][] laneMatrix) {
        this.laneMatrix = laneMatrix;
    }

    public void setLaneCenterMatrix(Coordinate[][] laneCenterMatrix) {
        this.laneCenterMatrix = laneCenterMatrix;
    }

    public void initLaneVehicles() {
        this.laneVehicles = new ArrayList<>();
        for (int i = 0; i<lanes; i++){
            this.laneVehicles.add(new ArrayList<MOBILVehicle>());
        }
    }

    public Coordinate[] getLaneCenterById(int id){
        if(laneCenterMatrix == null || laneCenterMatrix.length <= id) return null;
        return laneCenterMatrix[id];
    }

    public void setBoundary(double minLat, double maxLat, double minLon, double maxLon){
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    public double headDistance(Coordinate coordinate){
        return Math.pow((head.coordinate().x-coordinate.x), 2) + Math.pow((head.coordinate().y-coordinate.y), 2);
    }

    public boolean isThisLinkLane(Coordinate coordinate){
//        System.out.println("minlat: " + minLat + ", maxlat:" + maxLat + ", minlon: " + minLon + ", maxLon: " + maxLon);
//        System.out.println(coordinate);
        if (coordinate.x >= minLat && coordinate.x <= maxLat && coordinate.y >= minLon && coordinate.y <= maxLon){
            return true;
        }
        return false;
    }
}
