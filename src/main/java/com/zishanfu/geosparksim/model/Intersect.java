package com.zishanfu.geosparksim.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.util.LinkedList;
import java.util.Queue;

public class Intersect extends Point {
    private Queue<Vehicle> queue = new LinkedList<>();
    private Long wid;
    private static PrecisionModel precision = new PrecisionModel();

    public Intersect(Coordinate coordinate, int SRID, Long wid) {
        super(coordinate, precision, SRID);
        this.wid = wid;
    }

    private void addVehicle(Vehicle vehicle) {
        queue.add(vehicle);
    }

    private Vehicle pollVehicle(Vehicle vehicle) {
        if (queue.isEmpty()) return null;
        else return queue.poll();
    }

    public Long getWid() {
        return wid;
    }
}
