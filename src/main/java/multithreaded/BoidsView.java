package multithreaded;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class BoidsView implements ChangeListener, ActionListener {

    private JFrame frame;
    private BoidsPanel boidsPanel;
    private JSlider cohesionSlider, separationSlider, alignmentSlider;
    private BoidsModel model;
    private int width, height;
    private JButton startButton, stopButton, suspendButton;
    private BoidsSimulator controller;

    public BoidsView(BoidsModel model, int width, int height, BoidsSimulator sim) {
        this.model = model;
        this.width = width;
        this.height = height;
        this.controller = sim;

        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
        cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        suspendButton = new JButton("Suspend");
        suspendButton.addActionListener(this);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(suspendButton);
        buttonPanel.add(stopButton);


        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

        cp.add(BorderLayout.SOUTH, slidersPanel);
        cp.add(BorderLayout.NORTH, buttonPanel);

        frame.setContentPane(cp);

        frame.setVisible(true);
    }

    private JSlider makeSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(10, new JLabel("1"));
        labelTable.put(20, new JLabel("2"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }

    public void update(int frameRate) {
        boidsPanel.setFrameRate(frameRate);
        SwingUtilities.invokeLater(() -> boidsPanel.repaint());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == separationSlider) {
            var val = separationSlider.getValue();
            model.setSeparationWeight(0.1 * val);
        } else if (e.getSource() == cohesionSlider) {
            var val = cohesionSlider.getValue();
            model.setCohesionWeight(0.1 * val);
        } else {
            var val = alignmentSlider.getValue();
            model.setAlignmentWeight(0.1 * val);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            startButton.setEnabled(false);
            suspendButton.setEnabled(true);
            stopButton.setEnabled(true);
            try {
                this.controller.runSimulation();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

        } else if (e.getSource() == suspendButton) {
            this.controller.suspendSimulation();
        } else if (e.getSource() == stopButton) {
            this.controller.resetSimulation();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}
