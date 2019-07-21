package com.zishanfu.geosparksim.Model;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Exception.EdgeOutBoundaryException;
import com.zishanfu.geosparksim.Exception.VehicleOutExceptedPathException;
import com.zishanfu.geosparksim.Model.parameters.MOBIL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MOBILVehicle extends LaneChangeBase implements MOBIL {
    private double p = politenessFactor;
    private double b = maximumSafeDeceleration;
    private double a = thresholdAcceleration;
    private Random rand = new Random();

    public MOBILVehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath) {
        super(id, source, target, edgePath, costs, fullPath);
    }

    @Override
    public MOBILVehicle headwayCheck(Map<Long, List<Link>> edgeMap, Map<Long, TrafficLight> signalWayMap) {
        return super.headwayCheck(edgeMap, signalWayMap);
    }

    public Map<Long, List<Link>> basicMovement(IDMVehicle head, double interval, Map<Long, List<Link>> edgeMap){
        //check intersection and light
        //check this and next edge light and vehicle
        TrafficLight headLight = this.getHeadSignal();
//        if (headLight != null && haversine(headLight.getLocation(), this.getFront()) <= this.getCarLength() && headLight.getSignal() == 2){
//            this.stop();
//            return edgeMap;
//        }

        //check position and update edge idx
        int edgeIdx = this.getEdgeIndex();
        double cost = this.getCostById(edgeIdx);

        Link currentLink = this.getCurrentLink();

        if (head == null && headLight == null){
            updateVelocity(interval);
        }else if (head != null && headLight == null){
            updateVelocity(head, interval);
        }else if (head == null && headLight.getSignal() == 2){
            if (haversine(headLight.getLocation(), this.getFront()) <= this.getCarLength()){
                this.stop();
                return edgeMap;
            }else{
                updateVelocity(headLight, interval);
            }
        }else if (head != null && headLight.getSignal() == 2){
            if (haversine(head.getRear(), this.getFront()) >= haversine(headLight.getLocation(), this.getFront())){
                updateVelocity(headLight, interval);
            }else{
                updateVelocity(head, interval);
            }
        }


        //remove vehicle from last lane
        currentLink.removeVehicleFromLane(this, this.getCurrentLane());

        //check the possible lane
        int currentLane = this.getCurrentLane();

        double posi = this.getPosition();

        //check next possible position
        if(cost <= posi){
            //change edge
            int newEdgeIdx = edgeIdx;
            double newP = posi;
            double newCost = cost;

            while(newEdgeIdx < this.getEdgePath().length-1 && newCost <= newP && newP >= 0){
                newP = newP - newCost;
                newEdgeIdx ++;
                newCost = this.getCostById(newEdgeIdx);
            }

            posi = newP;
            if (newEdgeIdx >= this.getEdgePath().length){
                //arrive
                this.setArrive(true);
            }else{
                this.setEdgeIndex(newEdgeIdx);
            }

        }else{
            //stay
            this.setEdgeIndex(edgeIdx);
        }

        this.setPosition(posi);

        return born(edgeMap, "Running");
    }

    private boolean safetyCriterion(MOBILVehicle head, MOBILVehicle back){
        if (head == null) return true;
        boolean accelerationCriterion = back == null || back.getAcceleration() > -b;
        return accelerationCriterion && (head.getPosition() - this.getPosition() > head.getCarLength()) &&
                (back == null || this.getPosition() - back.getPosition() > this.getCarLength());
    }

    private int laneChange(Link currentLink){
        int currentLane = this.getCurrentLane();
        int lanes = currentLink.getLanes();
        int bias = currentLane + (rand.nextBoolean() ? 1 : -1);
        int newLane =  currentLane >= lanes? lanes: (bias >= lanes || bias < 0? currentLane: bias);
        if (newLane == currentLane) return currentLane;
        if (newLane == lanes) newLane--;

        List<MOBILVehicle> vehicleInNewLane = currentLink.getLaneVehicles().get(newLane);

        if(vehicleInNewLane.size() == 0){
            return newLane;
        }else{
            double pos = this.getPosition();
            MOBILVehicle head = vehicleInNewLane.get(0);
            MOBILVehicle back = vehicleInNewLane.get(0);
            double headPos = Double.POSITIVE_INFINITY;
            double backPos = Double.POSITIVE_INFINITY;
            for (int i = 1; i<vehicleInNewLane.size(); i++){
                MOBILVehicle vehicle = vehicleInNewLane.get(i);
                double vPos = vehicle.getPosition();
                if(vPos > pos && vPos - pos < headPos){
                    head = vehicle;
                    headPos = vPos - pos;
                }else if(vPos < pos && pos - vPos < backPos){
                    back = vehicle;
                    backPos = pos - vPos;
                }
            }
            if (incentiveCriterion(back) && safetyCriterion(head, back)){
                return newLane;
            }else{
                return currentLane;
            }
        }
    }

    private double directionBias = 2.0;

    //back vehicle in targeted lane
    private boolean incentiveCriterion(MOBILVehicle back){
        if (back == null) return true;
        double left = this.getAcceleration() - this.a;
        double distance = back.a - back.getAcceleration();
        double right = p*distance + a - directionBias;
        return left > 0 && (left > right);
    }

    private Map<Long, List<Link>> updateMap(Map<Long, List<Link>> edgeMap, long wid, Link currentLink){
        List<Link> list = new ArrayList<>();
        for(Link link: edgeMap.get(wid)){
            if(link.getHead().coordinate().equals(currentLink.getHead().coordinate()) &&
                    link.getTail().coordinate().equals(currentLink.getTail().coordinate())){
                list.add(currentLink);
            }else{
                list.add(link);
            }
        }
        edgeMap.put(wid, list);
        return edgeMap;
    }


    public Map<Long, List<Link>> born(Map<Long, List<Link>> edgeMap, String type){
        int edgeIndex = this.getEdgeIndex();
        long current = this.getEdgeByIdx(edgeIndex);

        Link currentLink = null;
        try {
            currentLink = this.getLinkByHead(edgeMap, current, edgeIndex);
        } catch (EdgeOutBoundaryException | VehicleOutExceptedPathException e) {
            return edgeMap;
        }

        //choose possible lane
        //get targeted lane vehicle

        int currentLane = laneChange(currentLink);
        //5 lane change to 2, iterative change to 2
        while(currentLane >= currentLink.getLanes()){
            currentLane--;
        }
        this.setCurrentLane(currentLane);

        Coordinate[] coordinate = currentLink.getLaneCenterById(currentLane);

        this.setCurrentLink(currentLink);

        if(type.equals("Baby")){
            this.setPosition(this.getCarLength());
            this.setRear(distance2Coordinate(0, coordinate[0], coordinate[1]));
            this.setFront(distance2Coordinate(this.getPosition(), coordinate[0], coordinate[1]));
        }else{
            this.setRear(distance2Coordinate(this.getPosition() - this.getCarLength(), coordinate[0], coordinate[1]));
            this.setFront(distance2Coordinate(this.getPosition(), coordinate[0], coordinate[1]));
        }

        currentLink.addVehicleToLane(this, currentLane);
        return updateMap(edgeMap, current, currentLink);
    }


}
