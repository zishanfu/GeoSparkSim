package com.zishanfu.geosparksim.Model.parameters;


import com.zishanfu.geosparksim.Model.Link;
import com.zishanfu.geosparksim.Model.MOBILVehicle;
import com.zishanfu.geosparksim.Model.TrafficLight;

import java.util.List;
import java.util.Map;

public interface IDM {
    double normalAcceleration = 2.5;
    double brakeDeceleration = 3;
    double reactionTime = 1;
    double safeDistance = 3;
}
