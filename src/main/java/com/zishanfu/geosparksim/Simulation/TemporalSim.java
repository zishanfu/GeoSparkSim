package com.zishanfu.geosparksim.Simulation;


import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Model.Link;
import com.zishanfu.geosparksim.Model.MOBILVehicle;
import com.zishanfu.geosparksim.Model.StepReport;
import com.zishanfu.geosparksim.Model.TrafficLight;
import org.apache.spark.sql.Dataset;

import java.util.*;

public class TemporalSim {

    /**
     * Simulation without Spark
     *
     * @param edges the edges dataset
     * @param signals the signals dataset
     * @param vehicles the vehicles dataset
     * @param steps the steps dataset
     * @param timestep the timestep
     * @return a list of step report
     */
    public List<StepReport> sim(Dataset<Link> edges, Dataset<TrafficLight> signals, Dataset<MOBILVehicle> vehicles,
                                int steps, double timestep){

        Random rand = new Random();

        List<Link> edgeList = edges.toJavaRDD().collect();
        List<TrafficLight> signalList = signals.toJavaRDD().collect();
        List<MOBILVehicle> vehiclesList = vehicles.toJavaRDD().collect();

        Map<Integer, List<TrafficLight>> signalNodeMap = new HashMap<>();

        for (TrafficLight light: signalList){
            int nid = light.getSRID();
            if(!signalNodeMap.containsKey(nid)){
                signalNodeMap.put(nid, new ArrayList<>());
            }
            signalNodeMap.get(nid).add(light);
        }

        Map<Long, List<Link>> edgeMap = new HashMap<>();
        for (Link edge: edgeList){

            if(!edgeMap.containsKey(edge.getId())){
                edgeMap.put(edge.getId(), new ArrayList<>());
            }

            //initialize lane information
            String[] laneStrings = edge.getLaneArray().split("#");
            Coordinate[][] laneMatrix = new Coordinate[laneStrings.length - 1][2];
            Coordinate[][] laneCenterMatrix = new Coordinate[laneStrings.length - 1][2];

            for (int i = 1; i<laneStrings.length; i++){
                String lane = laneStrings[i];
                String[] lane_items = lane.split("\\|");
                String[] headLine = lane_items[1].split(",");
                String[] tailLine = lane_items[2].split(",");

                Coordinate laneHead = new Coordinate(Double.parseDouble(headLine[0]), Double.parseDouble(headLine[1]));
                Coordinate laneTail = new Coordinate(Double.parseDouble(tailLine[0]), Double.parseDouble(tailLine[1]));

                String[] headCenter = lane_items[3].split(",");
                String[] tailCenter = lane_items[4].split(",");
                Coordinate laneCenterHead = new Coordinate(Double.parseDouble(headCenter[0]), Double.parseDouble(headCenter[1]));
                Coordinate laneCenterTail = new Coordinate(Double.parseDouble(tailCenter[0]), Double.parseDouble(tailCenter[1]));

                laneMatrix[i - 1] = new Coordinate[]{laneHead, laneTail};
                laneCenterMatrix[i - 1] = new Coordinate[] {laneCenterHead, laneCenterTail};
            }

            double minLat = Math.min(edge.getHead().coordinate().x, edge.getTail().coordinate().x);
            double maxLat = Math.max(edge.getHead().coordinate().x, edge.getTail().coordinate().x);
            double minLon = Math.min(edge.getHead().coordinate().y, edge.getTail().coordinate().y);
            double maxLon = Math.max(edge.getHead().coordinate().y, edge.getTail().coordinate().y);

            for(Coordinate[] coordinates: laneMatrix){
                for(int i = 0; i<coordinates.length; i++){
                    minLat = Math.min(coordinates[i].x, minLat);
                    maxLat = Math.max(coordinates[i].x, maxLat);
                    minLon = Math.min(coordinates[i].y, minLon);
                    maxLon = Math.max(coordinates[i].y, maxLon);
                }
            }

            edge.setLaneMatrix(laneMatrix);
            edge.setLaneCenterMatrix(laneCenterMatrix);
            edge.setBoundary(minLat,maxLat,minLon,maxLon);
            edge.initLaneVehicles();

            edgeMap.get(edge.getId()).add(edge);
        }


        List<StepReport> stepReports = new ArrayList<>();

        //Initialize Signals
        Map<Long, TrafficLight> signalWayMap = new HashMap<>();

        for (int nid: signalNodeMap.keySet()){
            List<TrafficLight> four = signalNodeMap.get(nid);
            if (four.size() < 4) continue;
            TrafficLight w1 = four.get(0);
            double w1_angle = Math.abs(edgeMap.get(w1.getWid()).get(0).getAngle());
            TrafficLight w2 = four.get(1);
            double w2_angle = Math.abs(edgeMap.get(w2.getWid()).get(0).getAngle());
            TrafficLight w3 = four.get(2);
            double w3_angle = Math.abs(edgeMap.get(w3.getWid()).get(0).getAngle());
            TrafficLight w4 = four.get(3);
            int select = rand.nextInt(2);
            w1.setTime(0);
            w2.setTime(0);
            w3.setTime(0);
            w4.setTime(0);
            int select_signal = select == 1? 2: 0;
            int other_signal = (select + 1) % 2 == 1? 2: 0;

            if(Math.abs(w3_angle - w1_angle) < 0.1){
                w1.setSignal(select_signal);
                w3.setSignal(select_signal);
                w2.setSignal(other_signal);
                w4.setSignal(other_signal);
            }else if(Math.abs(w3_angle - w2_angle) < 0.1){
                w3.setSignal(select_signal);
                w2.setSignal(select_signal);
                w1.setSignal(other_signal);
                w4.setSignal(other_signal);
            }else{
                w3.setSignal(select_signal);
                w4.setSignal(select_signal);
                w1.setSignal(other_signal);
                w2.setSignal(other_signal);
            }

            signalWayMap.put(w1.getWid(), w1);
            signalWayMap.put(w2.getWid(), w2);
            signalWayMap.put(w3.getWid(), w3);
            signalWayMap.put(w4.getWid(), w4);

            stepReports.add(new StepReport(0, w1.getLocation(), w1.getWid(), w1.getSignal(), w1.getTime()));
            stepReports.add(new StepReport(0, w2.getLocation(), w2.getWid(), w2.getSignal(), w2.getTime()));
            stepReports.add(new StepReport(0, w3.getLocation(), w3.getWid(), w3.getSignal(), w3.getTime()));
            stepReports.add(new StepReport(0, w4.getLocation(), w4.getWid(), w4.getSignal(), w4.getTime()));
        }

        //initialize vehicle position
        for (MOBILVehicle vehicle: vehiclesList){
            vehicle.initVehicle();
            edgeMap = vehicle.born(edgeMap, "Baby");
            stepReports.add(new StepReport(0, vehicle.getId(), vehicle.getFront(), vehicle.getRear(), vehicle.getSource(), vehicle.getTarget(), vehicle.getEdgePath(), vehicle.getCosts(), vehicle.getFullPath(),
                    vehicle.getEdgeIndex(), vehicle.getCurrentLane(), vehicle.getPosition(), vehicle.getVelocity(), vehicle.getCurrentLink(), vehicle.getHeadSignal()));
        }

        for (int i = 1; i<= steps; i++){
            //update signal
            for (long wid: signalWayMap.keySet()){
                TrafficLight light = signalWayMap.get(wid);
                light.next(1);
                signalWayMap.put(wid, light);
                stepReports.add(new StepReport(i, light.getLocation(), light.getWid(), light.getSignal(), light.getTime()));
            }

            int arrive = 0;
            for (MOBILVehicle vehicle: vehiclesList){
                if (vehicle.isArrive()){
                    arrive++;
                    if (arrive == vehiclesList.size()) break;
                }else{
                    MOBILVehicle head = vehicle.headwayCheck(edgeMap, signalWayMap);
                    edgeMap = vehicle.basicMovement(head, 1, edgeMap);
                    stepReports.add(new StepReport(i, vehicle.getId(), vehicle.getFront(), vehicle.getRear(), vehicle.getSource(), vehicle.getTarget(), vehicle.getEdgePath(), vehicle.getCosts(), vehicle.getFullPath(),
                            vehicle.getEdgeIndex(), vehicle.getCurrentLane(), vehicle.getPosition(), vehicle.getVelocity(), vehicle.getCurrentLink(), vehicle.getHeadSignal()));
                    //visVehicle.add(new Segment(new GeoPoint(vehicle.getFront().x, vehicle.getFront().y), new GeoPoint(vehicle.getRear().x, vehicle.getRear().y), Color.BLUE, 3));
                }
            }
        }


        return stepReports;
    }


//    private void checkEdgeMap(Map<Long, List<Link>> edgeMap){
//        for (long wid: edgeMap.keySet()){
//            for (Link link: edgeMap.get(wid)){
//                for(List<MOBILVehicle> veh: link.getLaneVehicles()){
//                    for (MOBILVehicle v: veh){
//                        System.out.print(v.getId() + ", ");
//                    }
//                }
//            }
//        }
//        System.out.println();
//    }

}

