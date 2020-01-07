package com.zishanfu.geosparksim;

import com.zishanfu.geosparksim.model.SimConfig;
import com.zishanfu.geosparksim.simulation.Core;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    @Autowired SparkSession ss;

    private static final String APP_TITLE = "GeoSparkSim";
    private static final Logger LOG = Logger.getLogger(SimulationService.class);

    public void start(SimConfig simConfig) {
        Core core = new Core();
        core.preprocess(ss, simConfig);
        core.simulation(ss, simConfig, APP_TITLE);
    }
}
