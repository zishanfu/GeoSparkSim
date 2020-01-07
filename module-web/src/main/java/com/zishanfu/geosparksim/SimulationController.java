package com.zishanfu.geosparksim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zishanfu.geosparksim.model.SimConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SimulationController {

    @Autowired SimulationService service;

    private static final Logger LOG = Logger.getLogger(SimulationController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/simulation")
    public boolean run(@RequestBody final String simConfig) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimConfig sc = objectMapper.readValue(simConfig, SimConfig.class);
        LOG.warn(sc);
        service.start(sc);
        return true;
    }
}
