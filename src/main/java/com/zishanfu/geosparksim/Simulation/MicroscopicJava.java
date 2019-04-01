package com.zishanfu.geosparksim.Simulation;


import com.zishanfu.geosparksim.Model.Link;
import com.zishanfu.geosparksim.Model.MOBILVehicle;
import com.zishanfu.geosparksim.Model.StepReport;
import com.zishanfu.geosparksim.Model.TrafficLight;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction2;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.datasyslab.geospark.enums.GridType;
import org.datasyslab.geospark.spatialRDD.SpatialRDD;

import java.util.Iterator;
import java.util.Random;


public class MicroscopicJava {
    public void sim(SparkSession sparkSession, Dataset<Link> edges, Dataset<TrafficLight> signals, Dataset<MOBILVehicle> vehicles) throws Exception {
        Random rand = new Random();
        int numPartition = 100;
        int repartition = 120; //2min
        int steps = 600; //1s per step 10min
        SpatialRDD<MOBILVehicle> vehicleRDD = new SpatialRDD<>();
        vehicleRDD.setRawSpatialRDD(vehicles.toJavaRDD());
        vehicleRDD.analyze();
        vehicleRDD.spatialPartitioning(GridType.KDBTREE, numPartition);
        JavaSparkContext sc = new JavaSparkContext(sparkSession.sparkContext());

        SpatialRDD<Link> edgeRDD = new SpatialRDD<>();
        edgeRDD.setRawSpatialRDD(edges.toJavaRDD());
        edgeRDD.analyze();
        edgeRDD.spatialPartitioning(vehicleRDD.getPartitioner());

        SpatialRDD<TrafficLight> signalRDD = new SpatialRDD<>();
        signalRDD.setRawSpatialRDD(signals.toJavaRDD());
        signalRDD.analyze();
        signalRDD.spatialPartitioning(vehicleRDD.getPartitioner());

//        SpatialRDD<Intersect> intersectRDD = new SpatialRDD<>();
//        intersectRDD.setRawSpatialRDD(intersects.toJavaRDD());
//        intersectRDD.analyze();
//        intersectRDD.spatialPartitioning(vehicleRDD.getPartitioner());


        Function3<Iterator<MOBILVehicle>, Iterator<Link>, Iterator<TrafficLight>, Iterator<StepReport>> temporalSim = new Function3<Iterator<MOBILVehicle>, Iterator<Link>, Iterator<TrafficLight>, Iterator<StepReport>>() {

            @Override
            public Iterator<StepReport> call(Iterator<MOBILVehicle> mobilVehicleIterator, Iterator<Link> linkIterator, Iterator<TrafficLight> trafficLightIterator) throws Exception {
                return null;
            }
        };

        FlatMapFunction2<Iterator<MOBILVehicle>, Iterator<Link>, StepReport> sim2 = new FlatMapFunction2<Iterator<MOBILVehicle>, Iterator<Link>, StepReport>() {
            @Override
            public Iterator<StepReport> call(Iterator<MOBILVehicle> mobilVehicleIterator, Iterator<Link> linkIterator) throws Exception {
                return null;
            }
        };




        vehicleRDD.spatialPartitionedRDD.zipPartitions(edgeRDD.spatialPartitionedRDD, sim2);

        //vehicleRDD.spatialPartitionedRDD.zipPartitions(edgeRDD.spatialPartitionedRDD, signalRDD.spatialPartitionedRDD, temporalSim);

//        List<TrafficLight> signalList = signals.javaRDD().collect();
//        Map<Long, TrafficLight> signalMap = new HashMap<>();
//        for (TrafficLight light: signalList){
//            int select = rand.nextInt(3);
//            light.setSignal(select);
//            light.setTime(0);
//            signalMap.put(light.getWid(), light);
//        }
//
//        List<Intersect> intersectList = intersects.javaRDD().collect();
//        Map<Long, Intersect> intersectMap = new HashMap<>();
//        for(Intersect intersect: intersectList){
//            intersectMap.put(intersect.getWid(), intersect);
//        }
//
//        List<Link> edgeList = edges.javaRDD().collect();
//        Map<Long, List<Link>> edgeMap = new HashMap<>();
//        for (Link edge: edgeList){
//            if(!edgeMap.containsKey(edge)){
//                edgeMap.put(edge.getId(), new ArrayList<>());
//            }
//            edgeMap.get(edge.getId()).add(edge);
//        }
//
//        Broadcast<Map<Long, TrafficLight>> broadcastLights = sc.broadcast(signalMap);
//        Broadcast<Map<Long, Intersect>> broadcastIntersects = sc.broadcast(intersectMap);
//        Broadcast<Map<Long, List<Link>>> broadcastEdges = sc.broadcast(edgeMap);
//
//        vehicleRDD.spatialPartitionedRDD.mapPartitions(vehicle -> {
//            Map<Long, TrafficLight> global_signals = broadcastLights.value();
//            Map<Long, Intersect> global_intersects = broadcastIntersects.value();
//
//        })

    }
}
