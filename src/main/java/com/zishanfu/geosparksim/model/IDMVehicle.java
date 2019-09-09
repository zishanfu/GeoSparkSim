package com.zishanfu.geosparksim.model;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.Map;

import com.zishanfu.geosparksim.exception.EdgeOutBoundaryException;
import com.zishanfu.geosparksim.exception.VehicleOutExceptedPathException;
import com.zishanfu.geosparksim.model.parameters.IDM;
import com.zishanfu.geosparksim.tools.Distance;
import org.apache.log4j.Logger;

public class IDMVehicle extends CarFollowBase implements IDM {
    private final static Logger LOG = Logger.getLogger(IDMVehicle.class);
    private double a = normalAcceleration;
    private double b = brakeDeceleration;
    private double t = reactionTime;
    private double s0 = safeDistance;
    private double speedLimit = 17.88;
    public Distance distance = new Distance();

    //Vehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath)
    public IDMVehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath) {
        super(id, source, target, edgePath, costs, fullPath);
    }

    public double getAcceleration(){
        return a;
    }

    public double getDeceleration(){
        return b;
    }

    public double getMinDistance(){
        return s0;
    }

    public double getReactionDistance(){
        return this.getVelocity()*t;
    }

    //check current lane and next lane
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
                this.setHeadSignal(light2);
            }else{
                this.setHeadSignal(light1);
            }

            if(head2 == null) return head1;
            if(head1 == null) return null;

            double distance1 = distance.haversine(head1.getFront(), this.getFront());
            double distance2 = distance.haversine(head2.getFront(), this.getFront());

            if(distance1 <= distance2){
                return head1;
            }else{
                return head2;
            }

        }else{
            this.setArrive(true);
        }

        this.setHeadSignal(light1);
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
            double dist = distance.haversine(vehicle.getFront(), this.getFront());
            if(dist < min && dist >= 0){
                closet = vehicle;
            }
        }

        return closet;
    }

    public void updateVelocity(TrafficLight signal, double interval){
        double aTemp = signalDerivative(this, signal);

        double pTemp = this.getPosition() + (this.getVelocity() + (0.5*aTemp*interval)) * interval;
        if(pTemp>this.getPosition()) this.setPosition(pTemp);

        this.setVelocity(this.getVelocity() + interval *aTemp);
        if(this.getVelocity()<=0) this.setVelocity(0);
    }

    //update velocity and position by head vehicle
    public void updateVelocity(IDMVehicle headVeh, double interval){
        double aTemp = derivative(this, headVeh);

        double pTemp = this.getPosition() + (this.getVelocity() + (0.5*aTemp*interval)) * interval;
        if(pTemp>this.getPosition()) this.setPosition(pTemp);

        this.setVelocity(this.getVelocity() + interval *aTemp);
        if(this.getVelocity()<=0) this.setVelocity(0);
    }

    public void updateVelocity(double interval){
        double aTemp =  leaderDerivative(this);
        double pTemp = this.getPosition() + (this.getVelocity() + (0.5*aTemp*interval)) * interval;
        if(pTemp>this.getPosition()) this.setPosition(pTemp);

        this.setVelocity(this.getVelocity() + interval *aTemp);
        if(this.getVelocity()<=0) this.setVelocity(0);
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
        double s = distance.haversine(signal.getLocation(), veh.getFront());

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

    public Coordinate distance2Coordinate(double dist, Coordinate head, Coordinate tail){
        double k = dist / distance.haversine(head, tail);
        double x = head.x + k * (tail.x - head.x);
        double y = head.y + k * (tail.y - head.y);
        return new Coordinate(x, y);
    }


}
