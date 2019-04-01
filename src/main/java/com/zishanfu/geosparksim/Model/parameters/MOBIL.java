package com.zishanfu.geosparksim.Model.parameters;


import com.zishanfu.geosparksim.Model.Vehicle;

public interface MOBIL {
    double politenessFactor = 0.3;
    double maximumSafeDeceleration = 4;
    double thresholdAcceleration = 0.4;
    Vehicle backVehicle(int lane);
}

