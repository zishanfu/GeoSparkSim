package com.zishanfu.geosparksim.generation;

import com.vividsolutions.jts.geom.Coordinate;
import com.zishanfu.geosparksim.model.Vehicle;
import com.zishanfu.geosparksim.shortestpath.Graph;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class CreateVehicles {
    private final static Logger LOG = Logger.getLogger(CreateVehicles.class);

    private double maxLen;
    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private Graph graph;

    public CreateVehicles(String[] args, Coordinate coor1, Coordinate coor2, double maxLen){
        this.graph = new Graph(args);
        this.maxLen = maxLen;
        minLat = Math.min(coor1.x, coor2.x);
        maxLat = Math.max(coor1.x, coor2.x);
        minLon = Math.min(coor1.y, coor2.y);
        maxLon = Math.max(coor1.y, coor2.y);
    }

    /**
     * Generate vehicles in 8 threads
     *
     * @param total the number of vehicles
     * @param type the vehicle generation type
     * @return a list of vehicles
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<Vehicle> multiple(int total, String type) throws InterruptedException, ExecutionException {
        int thread_num = 8;
        ExecutorService executor = Executors.newFixedThreadPool(thread_num);
        Collection<Callable<Vehicle[]>> tasks = new ArrayList<>();

        for (int i=0; i<thread_num; i++)
        {
            GenThread thread = new GenThread( minLon, minLat, maxLon, maxLat, graph, maxLen, total/thread_num, type);
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

