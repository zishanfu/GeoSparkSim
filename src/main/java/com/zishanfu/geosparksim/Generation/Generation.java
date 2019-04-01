package com.zishanfu.geosparksim.Generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.ShortestPath.Graph;
import com.zishanfu.geosparksim.Tools.SpatialRandom;


public class Generation {

    public Vehicle[] vehicleGeneration(Coordinate coor1, Coordinate coor2, Graph graph, double maxLen, String type, int total) {
        double minLat = Math.min(coor1.x, coor2.x);
        double maxLat = Math.max(coor1.x, coor2.x);
        double minLon = Math.min(coor1.y, coor2.y);
        double maxLon = Math.max(coor1.y, coor2.y);
        SpatialRandom spatialRand = new SpatialRandom( minLon, minLat, maxLon, maxLat, maxLen, graph);

        Vehicle[] vehicles = new Vehicle[total];

        for(int i = 0; i<total; i++) {
            vehicles[i] = computeVehicle(type, spatialRand, i, graph);
        }

        return vehicles;
    }

    private Vehicle computeVehicle(String type, SpatialRandom rand, int sid, Graph graph){
        double len = rand.spatialRandomLen();
        Coordinate src = null;
        Coordinate dest = null;
        if(type.contains("DSO")) {
            src = rand.spatialRandomNode();
            dest = rand.computeDestDSO(src, len);
        }else if(type.contains("NB")) {
            src = rand.computeSourceNB();
            dest = rand.computeDestinationNB(src, len);
        }

        Vehicle vehicle = graph.request(src, dest);

        //recompute to meet the total requests
        if(vehicle == null) {
            return computeVehicle(type, rand, sid, graph);
        }

        Long[] path = vehicle.getEdgePath();
        if(path == null || path.length <= 1) {
            return computeVehicle(type, rand, sid, graph);
        }

        return vehicle;
    }
}
