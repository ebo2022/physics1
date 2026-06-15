package org.example;

import java.util.Random;

public class RocketSim {
    public static final double g = 9.81;
    private final AirDensity.Result airDensity;
    private final double theta, tExpected, dt, m, Cd, A;
    private final int iterations;
    private final Random random = new Random();

    public RocketSim(double theta, double tExpected,
                     double dt, double m,
                     double Cd, double A, AirDensity.Result airDensity,
                     int iterations) {
        this.theta = Math.toRadians(theta);
        this.tExpected = tExpected;
        this.dt = dt;
        this.m = m;
        this.Cd = Cd;
        this.A = A;
        this.airDensity = airDensity;
        this.iterations = iterations;
    }

    public Result run() {
        double guessSpeed = 9.81 * this.tExpected / ((double)2.0F * Math.sin(this.theta));
        double guessRange = guessSpeed * Math.cos(this.theta) * this.tExpected;
        double guess1 = guessSpeed;
        double guess2 = 1.1 * guessSpeed;
        SingleResult result1 = this.runSingle(guessSpeed);
        SingleResult result2 = this.runSingle(guess2);
        SingleResult bestResult = result2;
        double bestGuessSpeed = guess2;
        double tolerance = 1.0E-4;
        boolean success = false;

        for(int i = 0; i < this.iterations && !(Math.abs(result2.error - result1.error) < 1.0E-8); ++i) {
            double nextGuess = guess2 - result2.error * (guess2 - guess1) / (result2.error - result1.error);
            SingleResult nextResult = this.runSingle(nextGuess);
            if (Math.abs(nextResult.error) < tolerance) {
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

    private double ax(double vx, double vy) {
        double v = Math.sqrt(vx * vx + vy * vy);
        return (double)-0.5F * this.airDensity.density() * this.Cd * this.A / this.m * v * vx;
    }

    private double ay(double vx, double vy) {
        double v = Math.sqrt(vx * vx + vy * vy);
        return -9.81 - (double)0.5F * this.airDensity.density() * this.Cd * this.A / this.m * v * vy;
    }

    public SingleResult runSingle(double v0) {
        double x = (double)0.0F;
        double y = (double)0.0F;
        double t = (double)0.0F;
        double lastX = x;
        double lastY = y;
        double vx = v0 * Math.cos(this.theta);

        for(double vy = v0 * Math.sin(this.theta); y >= (double)0.0F; t += this.dt) {
            lastX = x;
            lastY = y;
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
