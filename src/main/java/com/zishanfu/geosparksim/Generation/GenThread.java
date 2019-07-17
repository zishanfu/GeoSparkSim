package com.zishanfu.geosparksim.Generation;

import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.ShortestPath.Graph;
import java.util.concurrent.Callable;


public class GenThread implements Callable<Vehicle[]> {
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private Graph graph;
    private double maxLen;
    private Generation generation = new Generation();
    private int num;
    private String type;

    public GenThread(double minLon, double minLat, double maxLon, double maxLat, Graph graph, double maxLen, int num, String type){
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.graph = graph;
        this.maxLen = maxLen;
        this.num = num;
        this.type = type;
    }

    @Override
    public Vehicle[] call() throws Exception {
        Vehicle[] vehicles = generation.vehicleGeneration(minLon, minLat, maxLon, maxLat, graph, maxLen, type, num);
        return vehicles;
    }

}
