package com.zishanfu.geosparksim.Model;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.Map;

import com.zishanfu.geosparksim.Exception.EdgeOutBoundaryException;
import com.zishanfu.geosparksim.Exception.VehicleOutExceptedPathException;
import com.zishanfu.geosparksim.Model.parameters.IDM;
import org.apache.log4j.Logger;

public class IDMVehicle extends Vehicle implements IDM {
    private final static Logger LOG = Logger.getLogger(IDMVehicle.class);
    private double a = normalAcceleration;
    private double b = brakeDeceleration;
    private double t = reactionTime;
    private double s0 = safeDistance;
    private double v = 0;
    private double p = startPosition;
    private double speedLimit = 17.88;
    private TrafficLight headSignal;

    //Vehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath)
    public IDMVehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath) {
        super(id, source, target, edgePath, costs, fullPath);
    }

    public double getVelocity(){
        return v;
    }

    public void setVelocity(double v){
        this.v = v;
    }

    public double getAcceleration(){
        return a;
    }

    public double getDeceleration(){
        return b;
    }

    public double getReactionDistance(){
        return v*t;
    }

    public double getPosition(){
        return p;
    }

    public double getMinDistance(){
        return s0;
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

    //check current lane and next lane
    @Override
    public MOBILVehicle headwayCheck(Map<Long, List<Link>> edgeMap, Map<Long, TrafficLight> signalWayMap) {

        int edgeIndex = this.getEdgeIndex();
        int nextEdgeIndex = edgeIndex + 1 == this.getEdgePath().length? 0: edgeIndex + 1;

        long current_wid = this.getEdgeByIdx(edgeIndex);

        Link currentLink = this.getCurrentLink();

        TrafficLight light1 = signalWayMap.getOrDefault(current_wid, null);

        speedLimit = currentLink != null? mph2mps(currentLink.getSpeed()): speedLimit;

        MOBILVehicle head1 = checkClosetVehicle(currentLink);

        if(nextEdgeIndex != 0){
            long next_wid = this.getEdgeByIdx(nextEdgeIndex);
            TrafficLight light2 = signalWayMap.getOrDefault(next_wid, null);

            Link nextLink = null;
            try {
                nextLink = this.getLinkByHead(edgeMap, next_wid, nextEdgeIndex + 1);
            } catch (EdgeOutBoundaryException | VehicleOutExceptedPathException e) {
                //LOG.error(e.getMessage());
            }


            MOBILVehicle head2 = checkClosetVehicle(nextLink);

            if(light2 != null && light1 == null){
                headSignal = light2;
            }else{
                headSignal = light1;
            }

            if(head2 == null) return head1;
            if(head1 == null) return null;

            double distance1 = haversine(head1.getFront(), this.getFront());
            double distance2 = haversine(head2.getFront(), this.getFront());

            if(distance1 <= distance2){
                return head1;
            }else{
                return head2;
            }

        }else{
            this.setArrive(true);
        }

        headSignal = light1;
        return head1;
    }


    private MOBILVehicle checkClosetVehicle(Link link){
        if(link == null) return null;
        if(this.getCurrentLane() >= link.getLanes()) return null;

        List<MOBILVehicle> laneVehicles = link.getLaneVehicles().get(this.getCurrentLane());

        if(laneVehicles == null || laneVehicles.size() == 0){
            return null;
        }

        MOBILVehicle closet = null;
        double min = Double.POSITIVE_INFINITY;

        for(MOBILVehicle vehicle: laneVehicles){
            double distance = haversine(vehicle.getFront(), this.getFront());
            if(distance < min && distance >= 0){
                closet = vehicle;
            }
        }

        return closet;
    }

    public void updateVelocity(TrafficLight signal, double interval){
        double aTemp = signalDerivative(this, signal);

        double pTemp = p + (v + (0.5*aTemp*interval)) * interval;
        if(pTemp>p) p = pTemp;

        v = v + interval *aTemp;
        if(v<=0) v = 0;
    }

    //update velocity and position by head vehicle
    public void updateVelocity(IDMVehicle headVeh, double interval){
        double aTemp = derivative(this, headVeh);

        double pTemp = p + (v + (0.5*aTemp*interval)) * interval;
        if(pTemp>p) p = pTemp;

        v = v + interval *aTemp;
        if(v<=0) v = 0;
    }

    public void updateVelocity(double interval){
        double aTemp =  leaderDerivative(this);
        double pTemp = p + (v + (0.5*aTemp*interval)) * interval;
        if(pTemp>p) p = pTemp;
        v = v + interval *aTemp;
        if(v<0) v = 0;
    }

    private double signalDerivative(IDMVehicle veh, TrafficLight signal) {
        double vDot;
        double sStar;

        double a = veh.getAcceleration();
        double b = veh.getDeceleration();
        double r = veh.getReactionDistance();
        double v = veh.getVelocity();
        double s0 = veh.getMinDistance();
        double v0 = speedLimit;

        double deltaV = v - 0;
        double s = haversine(signal.getLocation(), veh.getFront());

        sStar = r+((v*deltaV)/(2*Math.sqrt(a+b)));
        if (sStar<0) sStar = 0;

        vDot = a * (1 - Math.pow((v/v0), 4)) - Math.pow(((s0+sStar)/(s)), 2);

        return vDot;
    }


    private double derivative(IDMVehicle veh, IDMVehicle headVeh) {
        double vDot;
        double sStar;

        double a = veh.getAcceleration();
        double b = veh.getDeceleration();
        double r = veh.getReactionDistance();
        double v = veh.getVelocity();
        double s0 = veh.getMinDistance();
        double v0 = speedLimit;

        double deltaV = v - (headVeh.getVelocity());
        double s = headVeh.getPosition() - headVeh.getCarLength() - veh.getPosition();

        sStar = r+((v*deltaV)/(2*Math.sqrt(a+b)));
        if (sStar<0) sStar = 0;

        vDot = a * (1 - Math.pow((v/v0), 4)) - Math.pow(((s0+sStar)/(s)), 2);

        return vDot;
    }

    private double leaderDerivative(IDMVehicle veh){
        double vDot;
        double sStar;

        double a = veh.getAcceleration();
        double b = veh.getDeceleration();
        double v = veh.getVelocity();
        double v0 = speedLimit;

        double s = veh.getCarLength() - veh.getPosition();

        sStar = ((v*v)/(2*Math.sqrt(a+b)));
        if (sStar<0) sStar = 0;

        vDot = a * (1 - Math.pow((v/v0), 4)) - Math.pow(((sStar)/(s)), 2);

        return vDot;
    }

    //mile per hour to meter per seconds
    private double mph2mps(double mph){
        double mile2meter = 1609.35;
        return mph* mile2meter /3600;
    }

    public Coordinate distance2Coordinate(double distance, Coordinate head, Coordinate tail){
        double k = distance / haversine(head, tail);
        double x = head.x + k * (tail.x - head.x);
        double y = head.y + k * (tail.y - head.y);
        return new Coordinate(x, y);
    }


}
