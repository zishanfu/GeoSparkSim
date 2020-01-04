package com.zishanfu.geosparksim;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SimulationService {

    @Autowired
    JavaSparkContext sc;

    public void start() {

    }

}
