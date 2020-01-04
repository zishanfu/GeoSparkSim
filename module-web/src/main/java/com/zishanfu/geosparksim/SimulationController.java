package com.zishanfu.geosparksim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SimulationController {

    @Autowired
    SimulationService service;

    @RequestMapping(method = RequestMethod.POST, path = "/simulation")
    public boolean run(@RequestParam(required = true) String params) {
        return true;
    }
}