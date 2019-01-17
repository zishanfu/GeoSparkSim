package com.zishanfu.vistrips.components.impl;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;

import com.zishanfu.vistrips.model.NewWaypoint;

public class SimTask extends TimerTask implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3925924455011898559L;
	JavaRDD<NewWaypoint> pRDD;
	private int round;
	private static Timer timer;
	private int limit;
	private final static Logger LOG = Logger.getLogger(SimTask.class);
	
	public SimTask(JavaRDD<NewWaypoint> pRDD, Timer timer, int routeLength) {
		this.pRDD = pRDD;
		this.timer = timer;
		this.limit = routeLength;
	}
	
	@Override
	public void run() {
		pRDD.foreach(p -> {
			LOG.warn(p.getCurPosByN(round));
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
