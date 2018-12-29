package com.zishanfu.vistrips.controller.ControllerImpl;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ResultController {
	
	private int txtRow = 8;
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
        textArea.setText("This text area shows the updates!\n");
        JScrollPane bottomPanel = new JScrollPane(textArea);
        return bottomPanel;
	}
}
