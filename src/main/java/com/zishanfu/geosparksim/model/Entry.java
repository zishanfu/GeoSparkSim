package com.zishanfu.geosparksim.model;

public class Entry {
    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;
    private int total;
    private String outputPath;
    private int step;
    private double timestep;
    private String type;
    private int partition;

    public Entry(){}

    public Entry(double lat1, double lon1, double lat2, double lon2, int total,
                 String outputPath, int step, double timestep, String type){
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
        this.total = total;
        this.outputPath = outputPath;
        this.step = step;
        this.timestep = timestep;
        this.type = type;
        this.partition = total/70;
    }

    public double getLat1() {
        return lat1;
    }

    public void setLat1(double lat1) {
        this.lat1 = lat1;
    }

    public double getLon1() {
        return lon1;
    }

    public void setLon1(double lon1) {
        this.lon1 = lon1;
    }

    public double getLat2() {
        return lat2;
    }

    public void setLat2(double lat2) {
        this.lat2 = lat2;
    }

    public double getLon2() {
        return lon2;
    }

    public void setLon2(double lon2) {
        this.lon2 = lon2;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
        this.partition = total/70;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getTimestep() {
        return timestep;
    }

    public void setTimestep(double timestep) {
        this.timestep = timestep;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    @Override
    public String toString() {
        return "\nP1: " + lat1 + ", " + lon1 + "\n" + "P2: " + lat2 + ", " + lon2 + "\n" +
                "Total: " + total + "\n" + "Steps: " + step + "\n" + "Timestep: " + timestep +
                "\n" + "Generation Type: " + type + "\n" + "Repartition Time: " +
                "Partition: " + partition + "\n" + "Output: " + outputPath + "\n";
    }
}
