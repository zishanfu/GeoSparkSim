package com.zishanfu.vistrips.sim.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
public class JFrame001 extends JFrame {
    private static final long serialVersionUID = -8298152118685661613L;
    private JPanel contentPane;
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame001 frame = new JFrame001();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * Create the frame.
     */
    public JFrame001() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 850, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                // g2绘制Line2D
                Shape s02 = new Line2D.Float(10, 150, 50, 150);
                g2.setColor(Color.BLACK);
                Stroke stroke = new BasicStroke(5);
                g2.setStroke(stroke);
                g2.draw(s02);
                // g2绘制Line2D, 使用BasicStroke
                Shape s03 = new Line2D.Float(10, 180, 150, 180);
                g2.setColor(Color.BLACK);
                Stroke stroke02 = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 20, 5 }, 10);
                g2.setStroke(stroke02);
                g2.draw(s03);
            }
        };
        panel.setPreferredSize(new Dimension(2000, 1000));
        scrollPane.setViewportView(panel);
        JPanel panelColumn = new JPanel();
        scrollPane.setColumnHeaderView(panelColumn);
        JLabel lblNewLabel = new JLabel("New label");
        panelColumn.add(lblNewLabel);;
        JPanel panelRow = new JPanel();
        scrollPane.setRowHeaderView(panelRow);
        JLabel lblNewLabel_1 = new JLabel("New label");
        panelRow.add(lblNewLabel_1);
    }
}