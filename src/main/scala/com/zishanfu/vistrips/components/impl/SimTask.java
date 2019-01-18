package com.zishanfu.vistrips.components.impl;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import com.zishanfu.vistrips.model.Vehicle;

public class SimTask extends TimerTask implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3925924455011898559L;
	JavaRDD<Vehicle> vehicles;
	private int round;
	private static Timer timer;
	private int limit;
	private final static Logger LOG = Logger.getLogger(SimTask.class);
	
	public SimTask(JavaRDD<Vehicle> vehicles, Timer timer, int routeLength) {
		this.vehicles = vehicles;
		this.timer = timer;
		this.limit = routeLength;
	}
	
	@Override
	public void run() {
		vehicles.foreach(p -> {
    		//System.out.println(p.getCurPosByN(round));
    		//System.out.println(p.getCoordinateN(round));
    	});
		if(round == limit) {
			timer.cancel();
			timer.purge();
		}
    	round++;
	}

}
