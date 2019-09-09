package com.zishanfu.geosparksim.trafficUI;


import com.zishanfu.geosparksim.model.Link;
import com.zishanfu.geosparksim.model.StepReport;
import com.zishanfu.geosparksim.trafficUI.model.Segment;
import com.zishanfu.geosparksim.trafficUI.model.GeoPoint;
import com.zishanfu.geosparksim.trafficUI.model.Point;
import com.zishanfu.geosparksim.model.SegmentNode;
import org.apache.spark.sql.Dataset;

import java.awt.*;
import java.util.List;

public class TrafficPanel {

    private String appTitle;

    public TrafficPanel(String appTitle){
        this.appTitle = appTitle;
    }

    public void run(Dataset<Link> edges, List<StepReport> reports){

        MapWindow window = new MapWindow(appTitle);
        window.setVisible(true);

        List<Link> links = edges.toJavaRDD().collect();

        for(int i = 0; i<links.size(); i++) {
            Link l = links.get(i);
            SegmentNode head = l.getHead();
            SegmentNode tail = l.getTail();

            Point headCenter = new Point(head.coordinate().x, head.coordinate().y);
            Point tailCenter = new Point(tail.coordinate().x, tail.coordinate().y);
            window.addSegment(new Segment(headCenter, tailCenter, Color.black));

            String[] lanes = l.getLaneArray().split("#");
            for (String lane: lanes){
                if(lane == null || lane.length() == 0) continue;
                String[] lane_items = lane.split("\\|");
                String[] headLine = lane_items[1].split(",");
                String[] tailLine = lane_items[2].split(",");
                Point laneHead = new Point(Double.parseDouble(headLine[0]), Double.parseDouble(headLine[1]));
                Point laneTail = new Point(Double.parseDouble(tailLine[0]), Double.parseDouble(tailLine[1]));
                window.addSegment(new Segment(laneHead, laneTail, Color.black));
            }
        }

        for (StepReport report: reports){
            int step = report.getStep();
            if (report.getVehicleId() == null){
                Color color = report.getSignal() == 0? Color.GREEN: report.getSignal() == 1? Color.yellow: Color.RED;
                window.addSignal(new GeoPoint(report.getSignalLocation().x, report.getSignalLocation().y, color), step);
            }else{
                window.addVehicle(new Segment(new GeoPoint(report.getVehicleFront().x, report.getVehicleFront().y),
                        new GeoPoint(report.getVehicleRear().x, report.getVehicleRear().y), Color.BLUE, 3), step);
            }
        }

        window.runSimulation();
    }

}
