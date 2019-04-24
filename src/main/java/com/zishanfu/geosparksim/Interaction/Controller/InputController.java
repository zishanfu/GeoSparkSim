package com.zishanfu.geosparksim.Interaction.Controller;

import com.zishanfu.geosparksim.Interaction.Handler.GenerateBtnHandler;
import com.zishanfu.geosparksim.Interaction.Handler.SimulationBtnHandler;
import org.apache.spark.sql.SparkSession;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;


public class InputController {

    public JPanel inputPanel;
    private String[] genTypes = { "Data-space oriented approach(DSO)", "Network-based approach(NB)"};
    private int top = 5, left = 4, bottom = 5, right = 4;
    private Insets i = new Insets(top, left, bottom, right);
    private String appTitle;
    private boolean distributed;

    public InputController(CompController cc, ResultController rc, SparkSession spark, String appTitle, boolean distributed) {
        this.inputPanel = inputPanel(cc, rc, spark, appTitle);
        this.distributed = distributed;
    }

    private JPanel inputPanel(final CompController cc, ResultController rc, SparkSession spark, String appTitle) {
        //Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel label = new JLabel("GeoSparkSim");


        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/icon/traffic.png")); // load the image to a imageIcon
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(40, 40,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg);  // transform it back
        JLabel imageLabel = new JLabel(imageIcon);

        label.setFont(new Font("Bookman", Font.BOLD, 20));


        JLabel numLabel = new JLabel("Number of Vehicles: ");
        JLabel simLabel = new JLabel("Simulation Steps: ");
        JLabel tsLabel = new JLabel("Time per Step: ");
        JLabel ouputLabel = new JLabel("Output Path: ");

        Checkbox chkArea = new Checkbox("Select an area");
        chkArea.setFont(numLabel.getFont());
        chkArea.setFocusable(false);
        Checkbox chkSpark = new Checkbox("Run in Apache Spark");
        chkSpark.setFont(numLabel.getFont());
        chkSpark.setFocusable(false);

        TextField num = new TextField(10);
        TextField sim = new TextField(10);
        TextField timestep = new TextField(10);
        TextField file = new TextField(15);

        JComboBox<String> genList = new JComboBox<>(genTypes);
        genList.setSelectedIndex(0);

        JButton sBtn = new JButton("Run Simulation");
        JButton vBtn = new JButton("Show Visualization");

        SimulationBtnHandler sbHandler = new SimulationBtnHandler(appTitle);
        GenerateBtnHandler gbHandler = new GenerateBtnHandler(cc.selAdaper, num, sim, timestep, file, sbHandler, rc.textArea, genList, spark, distributed);
        sBtn.addActionListener(gbHandler);
        vBtn.addActionListener(sbHandler);

        GridBagLayout layout = new GridBagLayout();
        topPanel.setAlignmentX(0);
        topPanel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = i;
        gbc.weightx=0.5;
        gbc.weighty=0.5;
        topPanel.add(label, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        topPanel.add(imageLabel, gbc);


        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        topPanel.add(chkArea, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        topPanel.add(chkSpark, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        topPanel.add(numLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        topPanel.add(num, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        topPanel.add(simLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        topPanel.add(sim, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        topPanel.add(tsLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        topPanel.add(timestep, gbc);

        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        topPanel.add(genList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 1;
        topPanel.add(ouputLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.gridwidth = 1;
        topPanel.add(file, gbc);

        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.gridwidth = 1;
        topPanel.add(sBtn, gbc);
        gbc.gridx = 1;
        gbc.gridy = 16;
        gbc.gridwidth = 1;
        topPanel.add(vBtn, gbc);

//        topPanel.setLayout(new FlowLayout());
//        topPanel.add(label);
//        topPanel.add(chkArea);
//        topPanel.add(num);
//        topPanel.add(sim);
//        topPanel.add(timestep);
//        topPanel.add(genList);
//        topPanel.add(gBtn);
//        topPanel.add(sBtn);

        genList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                genList.showPopup();
            }
        });

        chkArea.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                if (e.getStateChange() == 1) {
                    cc.mapViewer.setOverlayPainter(cc.selPainter);
                } else {
                    cc.mapViewer.setOverlayPainter(null);
                }
            }
        });

        chkSpark.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED){
                    gbHandler.setRunSpark(true);
                }else{
                    gbHandler.setRunSpark(false);
                }
            }
        });

        return topPanel;

    }


}
