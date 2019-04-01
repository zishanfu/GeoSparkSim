package com.zishanfu.geosparksim.Interaction.Handler;

import com.zishanfu.geosparksim.Model.Link;
import com.zishanfu.geosparksim.Model.StepReport;
import com.zishanfu.geosparksim.Interaction.Components.AttentionDialog;
import com.zishanfu.geosparksim.TrafficUI.TrafficPanel;
import org.apache.spark.sql.Dataset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class SimulationBtnHandler implements ActionListener{
    private Dataset<Link> edges;
    private List<StepReport> reports;
    private String appTitle;
    private boolean ui;

    public SimulationBtnHandler(String appTitle){
        this.appTitle = appTitle;
        this.ui = true;
    }

    public void actionPerformed(ActionEvent e) {
        if(edges == null || reports == null) {
            AttentionDialog dialog = new AttentionDialog("Attention",
                    "Simulation is not ready, please check!");
        }else if(!ui){
            AttentionDialog dialog = new AttentionDialog("Attention",
                    "Area too larger or too many vehicle, disabled traffic visualization!");
        } else {
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports);
        }
    }

    public void setEdges(Dataset<Link> edges) {
        this.edges = edges;
    }

    public void setReports(List<StepReport> reports) {
        this.reports = reports;
    }

    public void setUi(boolean ui) {
        this.ui = ui;
    }
}

