package com.zishanfu.vistrips.sim.model;

import com.zishanfu.vistrips.model.Vehicle;

public class MOBIL {
	private static final double POLITE = 0.3;
	private static final double MAX_SAFE_DEC = 18.6;
	private static final double THRESHOLD_ACC = 18.6;
	//MOBIL model
	//need improve on left and right turn 
	private double directionBias(int lane) {
		//do a target lane bias move and check 
//			if(roadLane > 1) {
//				
//			}else {
//				return 0.0;
//			}
			return 0.0;
	}
	
	private double safetyCriterion(Vehicle v) {
		return 0.0;
	}
}
