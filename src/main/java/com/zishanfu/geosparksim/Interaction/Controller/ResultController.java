package com.zishanfu.geosparksim.Interaction.Controller;

import javax.swing.*;
import java.awt.*;

public class ResultController {

    private int txtRow = 23;
    public JScrollPane resultPanel;
    public JTextArea textArea;

    public ResultController() {
        this.resultPanel = resultPanel();
    }

    public void setResults(String str) {
        textArea.setText(str);
    }

    public void appendResults(String str) {
        textArea.append(str);
    }

    private JScrollPane resultPanel() {
        //Result Panel
        this.textArea = new JTextArea();
        textArea.setRows(txtRow);
        textArea.setText("This text area shows geosparksim updates!\n");
        JScrollPane bottomPanel = new JScrollPane(textArea);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10,10,20,10));
        return bottomPanel;
    }
}
