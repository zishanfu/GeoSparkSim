package com.zishanfu.geosparksim.interaction.controller;

import com.zishanfu.geosparksim.interaction.handler.GenerateBtnHandler;
import com.zishanfu.geosparksim.interaction.handler.SimulationBtnHandler;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;

public class InputController {

    public JPanel inputPanel;
    private String[] genTypes = {"Data-space oriented approach(DSO)", "Network-based approach(NB)"};
    private int top = 5, left = 4, bottom = 5, right = 4;
    private Insets i = new Insets(top, left, bottom, right);
    private final Logger LOG = Logger.getLogger(InputController.class);

    public InputController(
            CompController cc, ResultController rc, SparkSession spark, String appTitle) {
        this.inputPanel = inputPanel(cc, rc, spark, appTitle);
    }

    private JPanel inputPanel(
            final CompController cc, ResultController rc, SparkSession spark, String appTitle) {
        // Top panel
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("GeoSparkSim");
        label.setFont(new Font("Bookman", Font.BOLD, 20));

        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/icon/traffic.png").getPath());

        Image image = imageIcon.getImage(); // transform it
        Image newimg =
                image.getScaledInstance(
                        40, 40, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg); // transform it back
        JLabel imageLabel = new JLabel(imageIcon);

        JLabel numLabel = new JLabel("Number of Vehicles: ");
        JLabel simLabel = new JLabel("Simulation Steps: ");
        JLabel tsLabel = new JLabel("Time per Step: ");
        JLabel ouputLabel = new JLabel("Output Path: ");

        Checkbox chkArea = new Checkbox("Select an area");
        chkArea.setFont(numLabel.getFont());
        chkArea.setFocusable(false);

        TextField num = new TextField(10);
        TextField sim = new TextField(10);
        TextField timestep = new TextField(10);
        TextField file = new TextField(15);

        JComboBox<String> genList = new JComboBox<>(genTypes);
        genList.setSelectedIndex(0);

        JButton sBtn = new JButton("Run Simulation");
        JButton vBtn = new JButton("Show Visualization");

        SimulationBtnHandler sbHandler = new SimulationBtnHandler(appTitle);
        GenerateBtnHandler gbHandler =
                new GenerateBtnHandler(
                        cc.selAdaper,
                        num,
                        sim,
                        timestep,
                        file,
                        sbHandler,
                        rc.textArea,
                        genList,
                        spark);
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
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
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
        gbc.gridwidth = 1;
        topPanel.add(numLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        topPanel.add(num, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        topPanel.add(simLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        topPanel.add(sim, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        topPanel.add(tsLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        topPanel.add(timestep, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        topPanel.add(genList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 1;
        topPanel.add(ouputLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.gridwidth = 1;
        topPanel.add(file, gbc);

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 1;
        topPanel.add(sBtn, gbc);
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.gridwidth = 1;
        topPanel.add(vBtn, gbc);

        genList.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        genList.showPopup();
                    }
                });
        chkArea.addItemListener(
                e -> {
                    if (e.getStateChange() == 1) {
                        cc.mapViewer.setOverlayPainter(cc.selPainter);
                    } else {
                        cc.mapViewer.setOverlayPainter(null);
                    }
                });

        return topPanel;
    }
}
