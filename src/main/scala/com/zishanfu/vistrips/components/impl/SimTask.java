package com.zishanfu.vistrips.components.impl;

import java.io.Serializable;
import java.util.TimerTask;

import org.apache.spark.api.java.JavaRDD;

import com.zishanfu.vistrips.model.NewWaypoint;

public class SimTask extends TimerTask implements Serializable{
	JavaRDD<NewWaypoint> pRDD;
	private int round;
	
	public SimTask(JavaRDD<NewWaypoint> pRDD) {
		this.pRDD = pRDD;
	}
	
	@Override
	public void run() {
		pRDD.foreach(p -> {
    		System.out.println(p.getCurPosByN(round));
    		//System.out.println(p.getCoordinateN(round));
    	});
    	round++;
	}

}
