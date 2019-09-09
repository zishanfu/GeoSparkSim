package com.zishanfu.geosparksim.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


public class TrafficLight extends Point{
    private long wid;
    private double time;
    private int signal; // 0 green 1 yellow 2 red
    private Coordinate location;

    private static PrecisionModel precision = new PrecisionModel();

    public TrafficLight(Coordinate coordinate, int SRID, long wid, Coordinate location) {
        super(coordinate, precision, SRID);
        this.wid = wid;
        this.location = location;
    }


    public Coordinate getLocation() {
        return location;
    }

    public long getWid() {
        return wid;
    }

    public double getTime() {
        return time;
    }

    public int getSignal() {
        return signal;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public void next(double dt){
        int duration = this.getSignal() == 0? 55: this.getSignal() == 1? 5: 60;
        double time = this.getTime() + dt;
        if (time < duration){
            this.setTime(time);
        }else{
            this.setSignal((this.getSignal() + 1) % 3);
            this.setTime(0);
        }
    }
}
