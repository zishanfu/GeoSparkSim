package com.zishanfu.geosparksim.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.zishanfu.geosparksim.exception.EdgeOutBoundaryException;
import com.zishanfu.geosparksim.exception.VehicleOutExceptedPathException;

import java.util.List;
import java.util.Map;


public class Vehicle extends Point {
    private String id;
    private Coordinate source;
    private Coordinate target;
    private Long[] edgePath;
    private Double[] costs;
    private List<Coordinate> fullPath; //coordinate = edge + 1
    private Coordinate front;
    private Coordinate rear;
    private int edgeIndex;
    private double carLength = 3;
    private int currentLane;
    private boolean isArrive;
    private Link currentLink;
    private static PrecisionModel precision = new PrecisionModel();

    //coordinate path length = edge path length + 1
    public Vehicle(String id, Coordinate source, Coordinate target, Long[] edgePath, Double[] costs, List<Coordinate> fullPath){
        super(source, precision, 1);
        this.id = id;
        this.source = source;
        this.target = target;
        this.edgePath = edgePath;
        this.costs = costs;
        this.fullPath = fullPath;
    }

    public String getId() {
        return id;
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

    public Long getEdgeByIdx(int idx){
        return edgePath[idx];
    }

    public String[] getCoordinateString(){
        String[] arr = new String[fullPath.size()];
        for(int i = 0; i<fullPath.size(); i++){
            Coordinate coordinate = fullPath.get(i);
            arr[i] = coordinate.toString();
        }
        return arr;
    }

    public Double[] getCosts() {
        return costs;
    }

    public Double getCostById(int id) {
        return costs[id];
    }

    public double getCarLength() {
        return carLength;
    }

    public List<Coordinate> getFullPath() {
        return fullPath;
    }

    public void initVehicle(){
        this.edgeIndex = 0;
        this.currentLane = 0;
        this.isArrive = false;
    }

    public boolean isArrive() {
        return isArrive;
    }

    public void setArrive(boolean arrive) {
        isArrive = arrive;
    }

    public Link getLinkByHead(Map<Long, List<Link>> edgeMap, long wid, int idx) throws EdgeOutBoundaryException, VehicleOutExceptedPathException {

        if (! edgeMap.containsKey(wid)){
            throw new EdgeOutBoundaryException("Edge " + wid + " is not in boundary. ");
        }

        List<Link> vlinks = edgeMap.get(wid);
        Coordinate coordinate = this.getFullPathN(idx);
        double distance = Double.POSITIVE_INFINITY;
        Link link = null;
        for(Link vlink: vlinks){
            double d = vlink.headDistance(coordinate);
            if(d < distance){
                link = vlink;
                distance = d;
            }
        }
        return link;
    }

    public Link getLinkByBoundary(Map<Long, List<Link>> edgeMap, long wid, Coordinate coordinate){
        List<Link> vlinks = edgeMap.get(wid);

        for(Link vlink: vlinks){
            if(vlink.isThisLinkLane(coordinate)){
                return vlink;
            }
        }
        return null;
    }

    public Coordinate getFullPathN(int idx){
        return fullPath.get(idx);
    }


    public int getEdgeIndex() {
        return edgeIndex;
    }

    public void setEdgeIndex(int edgeIndex) {
        this.edgeIndex = edgeIndex;
    }

    public Coordinate getRear() {
        return rear;
    }

    public Coordinate getFront() {
        return front;
    }

    public void setCurrentLane(int currentLane) {
        this.currentLane = currentLane;
    }

    public int getCurrentLane() {
        return currentLane;
    }

    public void setFront(Coordinate front) {
        this.front = front;
    }

    public void setRear(Coordinate rear) {
        this.rear = rear;
    }

    public void setCurrentLink(Link currentLink) {
        this.currentLink = currentLink;
    }

    public Link getCurrentLink() {
        return currentLink;
    }


    @Override
    public String toString() {
        //return "From: " + source.toString() + ", To: " + target.toString() + ", Edges: " + edgePath.toString() + ", Costs: " + costs.toString() + ", Coordinates: " + fullPath.toString();
        return "From: " + source.toString() + ", To: " + target.toString() + ", Rear: " + rear + ", Front: " + front;
    }
}
