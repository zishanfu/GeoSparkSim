package com.zishanfu.geosparksim.Generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.Model.Vehicle;
import com.zishanfu.geosparksim.ShortestPath.Graph;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class CreateVehicles {
    private final static Logger LOG = Logger.getLogger(CreateVehicles.class);

    private double maxLen;
    private Coordinate coor1;
    private Coordinate coor2;
    private Graph graph;

    public CreateVehicles(String[] args, Coordinate coor1, Coordinate coor2, double maxLen){
        this.graph = new Graph(args);
        this.maxLen = maxLen;
        this.coor1 = coor1;
        this.coor2 = coor2;
    }

    public List<Vehicle> multiple(int total, String type) throws InterruptedException, ExecutionException {
        int thread_num = 8;
        ExecutorService executor = Executors.newFixedThreadPool(thread_num);
        Collection<Callable<Vehicle[]>> tasks = new ArrayList<>();

        for (int i=0; i<thread_num; i++)
        {
            GenThread thread = new GenThread( coor1,coor2, graph, maxLen, total/thread_num, type);
            tasks.add(thread);
        }

        List<Future<Vehicle[]>> results = executor.invokeAll(tasks);
        awaitTerminationAfterShutdown(executor);
        List<Vehicle> trajectories = new ArrayList<>();

        for (int i = 0; i<results.size(); i++){
            Future<Vehicle[]> vehicles = results.get(i);
            if(!vehicles.isCancelled() && vehicles.isDone()){
                trajectories.addAll(Arrays.asList(vehicles.get()));
            }
        }

        return trajectories;

    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


}

