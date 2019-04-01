package com.zishanfu.geosparksim.Generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.ShortestPath.Graph;

import java.util.concurrent.Callable;


public class GenThread implements Callable<Vehicle[]> {
    private Coordinate coor1;
    private Coordinate coor2;
    private Graph graph;
    private double maxLen;
    private Generation generation = new Generation();
    private int num;
    private String type;

    public GenThread(Coordinate coor1, Coordinate coor2, Graph graph, double maxLen, int num, String type){
        this.coor1 = coor1;
        this.coor2 = coor2;
        this.graph = graph;
        this.maxLen = maxLen;
        this.num = num;
        this.type = type;
    }

    @Override
    public Vehicle[] call() throws Exception {
        Vehicle[] vehicles = generation.vehicleGeneration(coor1, coor2, graph, maxLen, type, num);
        return vehicles;
    }

}
