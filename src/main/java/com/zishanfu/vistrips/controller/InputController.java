package com.zishanfu.vistrips.controller;

import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.spark.sql.SparkSession;

import com.zishanfu.vistrips.components.GenerateBtnHandler;
import com.zishanfu.vistrips.components.SimulationBtnHandler;


public class InputController {
	
	public JPanel inputPanel;
	private String[] genTypes = { "Data-space oriented approach(DSO)", "Region-based approach(RB)", "Network-based approach(NB)"};
	
	public InputController(CompController cc, ResultController rc) {
		this.inputPanel = inputPanel(cc, rc);
	}
	
	private JPanel inputPanel(final CompController cc, ResultController rc) {
		//Top panel
        JPanel topPanel = new JPanel();
        JLabel label = new JLabel("VisTrips");
        Checkbox chkArea = new Checkbox("Select an area and enter the number of moving objects");
        chkArea.setFocusable(false);
        TextField num = new TextField(10);
        JButton gBtn = new JButton("Generate Scenario");
        JButton sBtn = new JButton("Open Simulatior");
        JComboBox genList = new JComboBox(genTypes);
        genList.setSelectedIndex(0);
        
    	SparkSession spark = SparkSession
  			  .builder()
  			  .master("local[*]")
  			  .appName("App")
  			  .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  	          .config("spark.kryo.registrator", "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
  			  .getOrCreate();
        
        SimulationBtnHandler sbHandler = new SimulationBtnHandler(cc.mapViewer);
        GenerateBtnHandler gbHandler = new GenerateBtnHandler(cc.selAdaper, num, sbHandler, rc.textArea, genTypes, genList, spark);
        gBtn.addActionListener(gbHandler);
        sBtn.addActionListener(sbHandler);

        topPanel.setLayout(new FlowLayout());
        topPanel.add(label);
        topPanel.add(chkArea);
        topPanel.add(num);
        topPanel.add(genList);
        topPanel.add(gBtn);
        topPanel.add(sBtn);
        
        chkArea.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		// TODO Auto-generated method stub
        		if(e.getStateChange()==1) {
        			cc.mapViewer.setOverlayPainter(cc.selPainter);
        		}else {
        			cc.mapViewer.setOverlayPainter(null);
        		}
        	}
        });
        
        return topPanel;
        
	}
}
