package com.zishanfu.vistrips.sim.model;

import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;

public class IDMVehicle extends Vehicle{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5983544134149356675L;
	//Reference : https://copradar.com/chapts/references/acceleration.html
	private static final double SPEED = 17.88; // m/s, 40.0mph, 40*1609/3600 m/s
	private static final double MAX_SPEED = SPEED;
	private static final double ACC = 2.0; // m/s^2
	private static final double DEC = 3.0; // m/s^2
	private static final double ACC_EXP = 4.0;
	private static final double MIN_GAP = 2.5; //2.5m

	private double dt = 2; //2s headway
	private double distScale = 0.0000021; // 1 meter in coordinate euclidean
	private double speed;
	private double acc = 0;
	private Report report = new Report(this.getSRID(), this.getLocation(),speed,acc);
	
	//current partition points
	/**
	 * @param Coordinate[] points
	 * @param int sid
	 * @param long time
	 * @param double distance
	 */
	public IDMVehicle(Coordinate[] points, int sid, long time, double distance) {
		super(points, sid, time, distance);
	}
	
	//0.2
	//caculate possible next move
	//distScale
	public Coordinate moveNext() {
		this.speed = moderateSpeed();
		double mySpeed = moderateSpeed() * distScale;
		double heading = this.getvBuffer().getHeadAngle();
		double dx = Math.cos(heading) * mySpeed * dt;
		double dy = Math.sin(heading) * mySpeed * dt;
		Coordinate coordinate = new Coordinate(this.getLocation().x + dx, this.getLocation().y + dy);
		this.report = new Report(this.getSRID(), coordinate, this.speed, this.acc);
		return coordinate;
	}
		
	//IDM model
	private double dynamicGap(IDMVehicle head) {
		double dv = this.speed - head.getSpeed();
		double gap = MIN_GAP + Math.max(0, this.speed * dt + this.speed*dv / (2 * Math.sqrt(ACC*DEC)));
		return gap;
	}
		
	private double acceleration(IDMVehicle head) {
		double ss = dynamicGap(head);
		double s = distanceTo.harversineMeter(head.getLocation(), this.getLocation());
		double a = ACC * (1- Math.pow(speed / SPEED, ACC_EXP) - Math.pow(ss/s, 2));
		return a;
	}

	private double moderateSpeed() {
		IDMVehicle v = getClosestAhead();
		if (v != null) {
			double a = acceleration(v);
			speed = Math.max(0, a*dt);
			this.acc = a;
		} else {
			speed = speed + dt * ACC;
			this.acc = ACC;
		}
		speed = Math.min(speed, MAX_SPEED);
		return speed;
	}	
	
	private IDMVehicle getClosestAhead() {
		Set<IDMVehicle> aheadVehicles = this.getAheadVehicles();
		if(aheadVehicles == null || aheadVehicles.size() == 0) {
			return null;
		}
		double min = Double.MAX_VALUE;
		IDMVehicle minV = null;
		for(IDMVehicle v: aheadVehicles) {
			double dist = this.getLocation().distance(v.getLocation());
			if(dist < min) {
				min = dist;
				minV = v;
			}
		}
		return minV;
	}

	public double getSpeed() {
		return speed;
	}

	public Report getReport() {
		return report;
	}	
	
}
