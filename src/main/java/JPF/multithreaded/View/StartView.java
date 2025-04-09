package JPF.multithreaded.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartView implements ActionListener {
    private final JFrame frame;
    private final JButton startButton;
    private final JTextField boidsCountField;
    private Integer result = null;

    public StartView(int width, int height) {
        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);


        JPanel panel = new JPanel();
        LayoutManager layout = new BorderLayout();
        panel.setLayout(layout);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Number of Boids:"));
        boidsCountField = new JTextField(5);
        boidsCountField.setText("1500");
        inputPanel.add(boidsCountField);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Simulation");
        startButton.addActionListener(this);
        buttonPanel.add(startButton);

        panel.add(BorderLayout.NORTH, inputPanel);
        panel.add(BorderLayout.CENTER, buttonPanel);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        try {
            result = Integer.parseInt(boidsCountField.getText());
            if (result > 0) {
                startButton.setEnabled(false);
                notifyAll();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Need a positive number of boids.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                result = null;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Need a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            result = null;
        }
    }

    public synchronized int getBoidCount() throws InterruptedException {
        while (result == null) {
            wait();
        }
        frame.dispose();
        return result;
    }

}
