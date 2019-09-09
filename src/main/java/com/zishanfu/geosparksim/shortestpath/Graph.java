package com.zishanfu.geosparksim.shortestpath;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The graph class leverage the APIs and index from GraphHopper to calculate the route with the minimum travel length.
 * GraphHopper is a route planning engine. To see more details about GraphHopper, https://www.graphhopper.com/
 */
public class Graph {
    private MyGraphHopper hopper;

    public Graph(String[] args){
        this.hopper = new MyGraphHopper();
        this.hopper.init(CmdArgs.read(args));
        this.hopper.importOrLoad(); 
    }

    /**
     *
     * @param from the source coordinate
     * @param to the destination coordinate
     * @return vehicle
     */
    public Vehicle request(Coordinate from, Coordinate to) {
        GHResponse rsp = new GHResponse();
        List<Path> paths = hopper.calcPaths(new GHRequest(from.x, from.y, to.x, to.y).
                setWeighting("fastest").setVehicle("car"), rsp);

        if(paths.size() == 0) return null;

        Path path0 = paths.get(0);
        int edge_count = path0.getEdgeCount();

        Long[] edgePath = new Long[edge_count];
        Double[] costs = new Double[edge_count];

        int idx = 0;

        List<Coordinate> fullPath = new ArrayList<>();
        fullPath.add(from);

        for (EdgeIteratorState edge : path0.calcEdges()) {
            int edgeId = edge.getEdge();
            if (edge instanceof VirtualEdgeIteratorState) {
                VirtualEdgeIteratorState vEdge = (VirtualEdgeIteratorState) edge;
                edgeId = vEdge.getOriginalTraversalKey() / 2;
            }
            edgePath[idx] = hopper.getOSMWay(edgeId);

            PointList points = edge.fetchWayGeometry(2);
            int pSize = points.size();
            fullPath.add(new Coordinate(points.getLat(pSize - 1), points.getLon(pSize - 1)));
            costs[idx] = edge.getDistance();
            idx++;
        }

        if(rsp.hasErrors() || fullPath.size() <= 1) {
            return null;
        }

        return new Vehicle(generateId(), from, to, edgePath, costs, fullPath);
    }

    private String generateId(){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random ID.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }


    /**
     * @return long totalNodes in GeoSparkSim.Graph
     */
    public long getTotalNodes() {
        return this.hopper.getGraphHopperStorage().getBaseGraph().getNodes();
    }


    /**
     *
     * @param node the node coordinate
     * @return the closest node coordinate
     */
    public Coordinate getClosestNode(Coordinate node) {
        GHPoint res = hopper.getLocationIndex().findClosest(node.x, node.y, EdgeFilter.ALL_EDGES).getQueryPoint();
        Coordinate newNode = new Coordinate(res.getLat(), res.getLon());
        return newNode;
    }
}
