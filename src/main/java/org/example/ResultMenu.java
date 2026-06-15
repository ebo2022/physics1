package org.example;

import org.scilab.forge.jlatexmath.TeXFormula;

import javax.swing.*;
import java.awt.*;

public class ResultMenu {
    private static int rowCount = 1;
    private boolean showSteps;

    public ResultMenu(RocketSim.Result results, boolean showSteps) {
        this.showSteps = showSteps;
        JFrame frame = new JFrame("rocket lab results");
        frame.setDefaultCloseOperation(3);
        frame.setSize(1200, 450);
        frame.setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = 2;
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(new JLabel("quantity"), c);
        c.gridx = 1;
        formPanel.add(new JLabel("value"), c);
        if (showSteps) {
            c.gridx = 2;
            formPanel.add(new JLabel("how calculated"), c);
            c.gridx = 3;
            formPanel.add(new JLabel("explanation"), c);
        }

        this.measurement("sat. vapor pressure (Pa)", "the estimated maximum pressure water vapor could exert based on temperature", true, "P_{sat}=610.94e^{\\frac{17.625T_c}{T_c+243.04}}", results.airDensity().pSat(), 2, c, formPanel);
        this.measurement("vapor partial pressure (Pa)", "the atmospheric pressure contribution from water vapor calculated from relative humidity", true, "P_v=H_rP_{sat}", results.airDensity().pV(), 2, c, formPanel);
        this.measurement("dry air partial pressure (Pa)", "the atmospheric pressure contribution from everything else", true, "P_d=P-P_v", results.airDensity().pD(), 2, c, formPanel);
        this.measurement("air density (kg/m^3)", "the predicted air density from summing dry and moist densities from ideal gas law", true, "\\rho=\\frac{P_d}{R_dT}+\\frac{P_v}{R_vT}", results.airDensity().density(), 2, c, formPanel);
        this.measurement("initial speed 1st guess (m/s)", "the rocket speed calculated from normal kinematics (no drag) and first speed guess for drag-based solver", true, "v_0=\\frac{gt}{2\\sin\\theta}", results.guessSpeed(), 2, c, formPanel);
        this.measurement("range 1st guess (m)", "the rocket range calculated from normal kinematics (no drag)", true, "x=(v_0\\cos\\theta)t", results.guessRange(), 2, c, formPanel);
        this.measurement("initial speed refined guess (m/s)", "the new rocket speed from drag solver", false, "secant method error solver", results.refinedSpeed(), 2, c, formPanel);
        this.measurement("range refined guess (m)", "the new rocket firing range from drag solver", false, "secant method error solver", results.refinedRange(), 2, c, formPanel);
        //this.measurement("time error (%)", "the percent error b/w simulated and experimental time; should be negligible ideally", true, "PE=\\frac{t_{sim}-t_{exp}}{t_{exp}}\\times 100", results.timePercentError(), 5, c, formPanel);
        frame.add(formPanel, "Center");
        frame.setVisible(true);
    }

    private void measurement(String name, String explanation, boolean latex, String desc, double val, int places, GridBagConstraints c, JPanel panel) {
        c.gridx = 0;
        c.gridy = rowCount;
        panel.add(new JLabel(name), c);
        c.gridx = 1;
        // formatted string to display rounded values
        String formatKey = "%." + places + "f";
        panel.add(new JLabel(String.format(formatKey, val)), c);
        if (this.showSteps) {
            c.gridx = 2;
            panel.add(latex ? new JLabel((new TeXFormula(desc)).createTeXIcon(0, 14.0F)) : new JLabel(desc), c);
            c.gridx = 3;
            panel.add(new JLabel(explanation), c);
        }

        ++rowCount;
    }
}
