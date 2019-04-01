package com.zishanfu.geosparksim.Tools;


import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.ShortestPath.Graph;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class SpatialRandom{

    private Random rand = new Random();
    private double minLon;
    private double minLat;
    private double maxLon;
    private double maxLat;
    private double minLen = 0.004; //1.9km
    private double maxLen;
    private Graph graph;

    public SpatialRandom(double minLon, double minLat, double maxLon, double maxLat,
                         double maxLen, Graph graph) {
        this.minLon = minLon;
        this.minLat = minLat;
        this.maxLon = maxLon;
        this.maxLat = maxLat;
        this.maxLen = maxLen;
        this.graph = graph;
    }

    public double randomRange(double low, double high) {
        return rand.nextDouble()*(high - low) + low;
    }

    public Coordinate spatialRandomNode() {
        Coordinate node = new Coordinate(randomRange(minLat, maxLat), randomRange(minLon, maxLon));
        return node;
    }

    public double spatialRandomLen() {
        double r = Math.abs(rand.nextGaussian());
        double lenDiff = maxLen - minLen;
        double len = lenDiff <= 0? minLen: (r * (maxLen - minLen) + minLen);
        return len;
    }

    public Coordinate computeDestDSO(Coordinate src, double len) {
        double angle = ThreadLocalRandom.current().nextDouble(360);
        double lon = src.y + len * Math.sin(angle);
        double lat = src.x + len * Math.cos(angle);
        return new Coordinate(lat, lon);
    }

    public Coordinate computeSourceNB() {
        Coordinate src = spatialRandomNode();
        return graph.getClosestNode(src);
    }

    public Coordinate computeDestinationNB(Coordinate src, double len) {
        Coordinate dest = computeDestDSO(src, len);
        return graph.getClosestNode(dest);
    }

}


