package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.Format;
import java.text.NumberFormat;

public class RunMenu {
    private JFormattedTextField angle;
    private JFormattedTextField time;
    private JFormattedTextField timeStep;
    private JFormattedTextField mass;
    private JFormattedTextField dragCoeff;
    private JFormattedTextField crossSectionArea;
    private JFormattedTextField iterations;
    private JFormattedTextField temperature;
    private JFormattedTextField pressure;
    private JFormattedTextField relativeHumidity;
    private JCheckBox showSteps;
    private JFrame frame = new JFrame("rocket lab");
    private static int rowCount = 0;

    public RunMenu() {
        this.frame.setDefaultCloseOperation(3);
        this.frame.setSize(400, 450);
        this.frame.setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = 2;
        this.option("angle (deg)", this.angle = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("time step (s)", this.timeStep = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("time in air from experiment (s)", this.time = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("mass (kg)", this.mass = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("drag coefficient", this.dragCoeff = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("cross-sectional area (m^2)", this.crossSectionArea = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("speed solver iterations", this.iterations = new JFormattedTextField(NumberFormat.getIntegerInstance()), c, formPanel);
        this.option("air temp (celsius)", this.temperature = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("air press (hPa)", this.pressure = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("relative humidity (%)", this.relativeHumidity = new JFormattedTextField(getFormat()), c, formPanel);
        this.option("show all steps?", this.showSteps = new JCheckBox("steps"), c, formPanel);
        this.frame.add(formPanel, "Center");
        JButton runButton = new JButton("Run");
        runButton.addActionListener(this::onRun);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(runButton);
        this.frame.add(bottomPanel, "South");
        this.frame.setVisible(true);
    }

    private static Format getFormat() {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(6);
        return format;
    }

    private void option(String name, JComponent option, GridBagConstraints c, JPanel panel) {
        c.gridx = 0;
        c.gridy = rowCount;
        panel.add(new JLabel(name + ":"), c);
        c.gridx = 1;
        panel.add(option, c);
        ++rowCount;
    }

    private void onRun(ActionEvent event) {
        if (this.isValidState()) {
            JButton sourceButton = (JButton) event.getSource();
            boolean doSteps = showSteps.isSelected();
            sourceButton.setEnabled(false); // prevent double clicks
            SwingWorker<RocketSim.Result, Void> worker = new SwingWorker<>() {
                @Override
                protected RocketSim.Result doInBackground() {
                    return new RocketSim(
                            ((Number) angle.getValue()).doubleValue(),
                            ((Number)time.getValue()).doubleValue(),
                            ((Number)timeStep.getValue()).doubleValue(),
                            ((Number)mass.getValue()).doubleValue(),
                            ((Number)dragCoeff.getValue()).doubleValue(),
                            ((Number)crossSectionArea.getValue()).doubleValue(),
                            AirDensity.airDensity(((Number)temperature.getValue()).doubleValue() + 273.15, ((Number)pressure.getValue()).doubleValue() * (double)100.0F, ((Number)relativeHumidity.getValue()).doubleValue() / (double)100.0F),
                            ((Number)iterations.getValue()).intValue())
                            .run();
                }

                @Override
                protected void done() {
                    sourceButton.setEnabled(true);
                    try {
                        RocketSim.Result result = get();
                        new ResultMenu(result, doSteps);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }

    }

    public boolean isValidState() {
        return this.angle.getValue() != null && this.time.getValue() != null && this.timeStep != null && this.mass.getValue() != null && this.dragCoeff.getValue() != null && this.crossSectionArea.getValue() != null && this.iterations.getValue() != null && this.temperature.getValue() != null && this.pressure.getValue() != null && this.relativeHumidity.getValue() != null;
    }
}
