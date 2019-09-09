package com.zishanfu.geosparksim.model;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.List;
import java.util.Map;

public abstract class CarFollowBase extends Vehicle{
    private double v = 0;
    private double p = 0;
    private TrafficLight headSignal;

    public CarFollowBase(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath) {
        super(id, source, target, edgePath, costs, fullPath);
    }

    public void recoverStatus(Coordinate front, Coordinate rear, int edgeIdx, int currentLane, double currentPois, double velocity, Link currentLink, TrafficLight light){
        this.setFront(front);
        this.setRear(rear);
        this.setEdgeIndex(edgeIdx);
        this.setCurrentLane(currentLane);
        this.setPosition(currentPois);
        this.setVelocity(velocity);
        this.setCurrentLink(currentLink);
        this.setHeadSignal(light);
    }

    public double getVelocity(){
        return v;
    }

    public void setVelocity(double v){
        this.v = v;
    }

    public double getPosition(){
        return p;
    }

    public void setPosition(double posi) {
        p = posi;
    }

    public void stop() {
        v = 0;
    }

    public TrafficLight getHeadSignal() {
        return headSignal;
    }

    public void setHeadSignal(TrafficLight headSignal) {
        this.headSignal = headSignal;
    }

    public abstract Vehicle headwayCheck(Map<Long, List<Link>> edgeMap, Map<Long, TrafficLight> signalWayMap);

}
