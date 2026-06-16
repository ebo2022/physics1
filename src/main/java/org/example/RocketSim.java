package org.example;

import java.util.Random;

/**
 * MAIN CLASS FOR ROCKET SIMULATION
 */
public class RocketSim {
    public static final double g = 9.81; // little g
    private final AirDensity.Result airDensity; // air density
    private final double theta, // angle
            tExpected, // experimental time
            dt, // simulation time step
            m, // mass
            Cd, // drag coefficient
            A; // cross-sectional area
    private final int iterations; // error solver iterations

    public RocketSim(double theta, double tExpected,
                     double dt, double m,
                     double Cd, double A, AirDensity.Result airDensity,
                     int iterations) {
        this.theta = Math.toRadians(theta); // input is in degrees, need radians
        this.tExpected = tExpected;
        this.dt = dt;
        this.m = m;
        this.Cd = Cd;
        this.A = A;
        this.airDensity = airDensity;
        this.iterations = iterations;
    }

    /**
     * runs a number of simulated trajectories to fit to the experimental time
     * <p><i>it is assumed time for vertical launch = time for angled launch which isn't actually true</i></p>
     * @return fitted results from the simulation
     */
    public Result run() {

        // the speed as would be calculated from the "normal" kinematic derivation
        // it also happens to be a good enough first guess for the error solver
        double guessSpeed = 9.81 * this.tExpected / ((double)2.0F * Math.sin(this.theta));

        // range calculated based on speed above; really just for results reporting, doesn't affect the simulation
        double guessRange = guessSpeed * Math.cos(this.theta) * this.tExpected;

        // 2 guesses
        double guess1 = guessSpeed;
        double guess2 = 1.1 * guessSpeed; // slightly perturb the guess speed for initial comparison

        // initialize with two scenario trajectories
        SingleResult result1 = this.runSingle(guessSpeed);
        SingleResult result2 = this.runSingle(guess2);
        SingleResult bestResult = result2;

        // the speed that currently best fits experimental time (i.e minimum error)
        double bestGuessSpeed = guess2;

        // acceptable amount of error to stop the simulation
        double tolerance = 1.0E-4;

        // flag for if the simulation loop because error went below tolerance
        boolean success = false;

        // MAIN ITERATION LOOP - repeatedly refines trajectory and minimizes error
        for(int i = 0; i < this.iterations; ++i) {

            // no point in keeping the loop going if error is basically zero
            if (Math.abs(result2.error - result1.error) < 1.0E-8) break;

            // secant-based approximation from current errors
            double nextGuess = guess2 - result2.error * (guess2 - guess1) / (result2.error - result1.error);

            // run a simulation with the new guess
            SingleResult nextResult = this.runSingle(nextGuess);

            if (Math.abs(nextResult.error) < tolerance) { // check if error is below the tolerance
                // update results to show new best and 2nd-best guesses
                bestResult = nextResult;
                bestGuessSpeed = nextGuess;
                success = true;
                break;
            }

            guess1 = guess2;
            result1 = result2;
            guess2 = nextGuess;
            result2 = nextResult;
        }

        if (!success) {
            bestResult = result2;
            bestGuessSpeed = guess2;
        }

        return new Result(this.airDensity, guessSpeed, guessRange, bestGuessSpeed, bestResult.range);
    }

    /**
     * calculates instantaneous acceleration in horizontal direction
     *
     * @param vx current velocity in x-direction (for drag force)
     * @param vy current velocity in y-direction (for drag force)
     * @return new x acceleration for the next time interval
     */
    private double ax(double vx, double vy) {
        double v = Math.sqrt(vx * vx + vy * vy);
        // drag will affect horizontal velocity unlike an ideal model; isolate just the horizontal vector component
        return -0.5 * this.airDensity.density() * this.Cd * this.A / this.m * v * vx;
    }

    /**
     * calculates instantaneous acceleration in vertical direction
     *
     * @param vx current velocity in x-direction (for drag force)
     * @param vy current velocity in y-direction (for drag force)
     * @return new y acceleration for the next time interval
     */
    private double ay(double vx, double vy) {
        double v = Math.sqrt(vx * vx + vy * vy);
        // two forces: gravity as a baseline plus any drag in the vertical direction
        return -g - 0.5 * this.airDensity.density() * this.Cd * this.A / this.m * v * vy;
    }

    /**
     * simulates a single rocket trajectory using RK4 (runge-kutta) numeric integration
     *
     * @param v0 speed to run simulation based on
     * @return record of relevant simulation results
     */
    public SingleResult runSingle(double v0) {
        // simulation parameters; all are they are labeled
        double x = 0, y = 0, t = 0;

        // keep previous x and y values to interpolate at end
        double lastX = x;
        double lastY = y;
        double vx = v0 * Math.cos(this.theta);

        for(double vy = v0 * Math.sin(this.theta); y >= 0; t += this.dt) {
            lastX = x;
            lastY = y;

            // runge-kutta 4 integration; this is equivalent in function to Euler's method
            // but takes a weighted average of derivative values instead for much greater accuracy
            // see https://rosettacode.org/wiki/Runge-Kutta_method#Java for what the code represents mathematically
            double k1vx = this.ax(vx, vy) * this.dt;
            double k1vy = this.ay(vx, vy) * this.dt;
            double k1x = vx * this.dt;
            double k1y = vy * this.dt;
            double k2vx = this.ax(vx + k1vx / (double)2.0F, vy + k1vy / (double)2.0F) * this.dt;
            double k2vy = this.ay(vx + k1vx / (double)2.0F, vy + k1vy / (double)2.0F) * this.dt;
            double k2x = (vx + k1vx / (double)2.0F) * this.dt;
            double k2y = (vy + k1vy / (double)2.0F) * this.dt;
            double k3vx = this.ax(vx + k2vx / (double)2.0F, vy + k2vy / (double)2.0F) * this.dt;
            double k3vy = this.ay(vx + k2vx / (double)2.0F, vy + k2vy / (double)2.0F) * this.dt;
            double k3x = (vx + k2vx / (double)2.0F) * this.dt;
            double k3y = (vy + k2vy / (double)2.0F) * this.dt;
            double k4vx = this.ax(vx + k3vx, vy + k3vy) * this.dt;
            double k4vy = this.ay(vx + k3vx, vy + k3vy) * this.dt;
            double k4x = (vx + k3vx) * this.dt;
            double k4y = (vy + k3vy) * this.dt;
            vx += (k1vx + (double)2.0F * k2vx + (double)2.0F * k3vx + k4vx) / (double)6.0F;
            vy += (k1vy + (double)2.0F * k2vy + (double)2.0F * k3vy + k4vy) / (double)6.0F;
            x += (k1x + (double)2.0F * k2x + (double)2.0F * k3x + k4x) / (double)6.0F;
            y += (k1y + (double)2.0F * k2y + (double)2.0F * k3y + k4y) / (double)6.0F;
        }

        // interpolate between last two positions to better capture where rocket hits ground
        double a = lastY / (lastY - y);
        double finalX = lastX + a * (x - lastX);
        return new SingleResult(t, finalX, t - this.tExpected);
    }

    public record Result(
            AirDensity.Result airDensity,
            double guessSpeed,
            double guessRange,
            double refinedSpeed,
            double refinedRange) {}

    public record SingleResult(
            double time,
            double range,
            double error
    ) {}
}
