package com.zishanfu.geosparksim.Model;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.List;

public class StepReport implements Serializable {
    private int step;
    private String vehicleId;
    private Coordinate vehicleFront;
    private Coordinate vehicleRear;
    private Coordinate signalLocation;
    private int signal;
    private long wid;
    private double time;
    private Coordinate source;
    private Coordinate target;
    private Long[] edgePath;
    private Double[] costs;
    private List<Coordinate> fullPath;
    private int edgeIdx;
    private int currentLane;
    private double position;
    private double velocity;
    private Link currentLink;
    private TrafficLight headSignal;

    public StepReport(int step, Coordinate signalLocation, int signal, String vehicleId, Coordinate vehicleFront, Coordinate vehicleRear){
        this.step = step;
        this.signalLocation = signalLocation;
        this.signal = signal;
        this.vehicleId =vehicleId;
        this.vehicleFront = vehicleFront;
        this.vehicleRear = vehicleRear;
    }

    public StepReport(int step, Coordinate signalLocation, long wid, int signal, double time){
        this.step = step;
        this.signalLocation = signalLocation;
        this.wid = wid;
        this.signal = signal;
        this.time = time;
    }

    public StepReport(int step, String vehicleId, Coordinate vehicleFront, Coordinate vehicleRear, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath, int edgeIdx, int currentLane, double position, double velocity, Link currentLink, TrafficLight headLight){
        this.step = step;
        this.vehicleId =vehicleId;
        this.vehicleFront = vehicleFront;
        this.vehicleRear = vehicleRear;
        this.source = source;
        this.target = target;
        this.edgePath = edgePath;
        this.costs = costs;
        this.fullPath = fullPath;
        this.edgeIdx = edgeIdx;
        this.currentLane = currentLane;
        this.position = position;
        this.velocity = velocity;
        this.currentLink = currentLink;
        this.headSignal = headLight;
    }

    public int getStep() {
        return step;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Coordinate getVehicleFront() {
        return vehicleFront;
    }

    public Coordinate getVehicleRear() {
        return vehicleRear;
    }

    public Coordinate getSignalLocation() {
        return signalLocation;
    }

    public int getSignal() {
        return signal;
    }

    public long getWid() {
        return wid;
    }

    public double getTime() {
        return time;
    }

    public int getEdgeIdx() {
        return edgeIdx;
    }

    public int getCurrentLane() {
        return currentLane;
    }

    public double getPosition() {
        return position;
    }

    public double getVelocity() {
        return velocity;
    }

    public Link getCurrentLink() {
        return currentLink;
    }

    public TrafficLight getHeadSignal() {
        return headSignal;
    }

    public Coordinate getSource() {
        return source;
    }

    public Coordinate getTarget() {
        return target;
    }

    public Long[] getEdgePath() {
        return edgePath;
    }

    public Double[] getCosts() {
        return costs;
    }

    public List<Coordinate> getFullPath() {
        return fullPath;
    }

    @Override
    public String toString() {
        return step + ", " + vehicleId + ", " + vehicleFront + ", " + vehicleRear + ", " + signalLocation + ", " + signal +
                ", " + wid + ", " + time + ", " + edgeIdx + ", " + currentLane + ", " + position + ", " + velocity + ", " + currentLink + ", " + headSignal;
    }
}
