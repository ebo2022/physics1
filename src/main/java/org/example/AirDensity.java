package org.example;

public class AirDensity {

    // gas constants (J/(kg·K))
    private static final double R_DRY_AIR = 287.05;
    private static final double R_WATER_VAPOR = 461.5;

    private static double saturationVaporPressure(double tempCelsius) {
        return 610.94 * Math.exp((17.625 * tempCelsius) / (tempCelsius + 243.04));
    }

    public static Result airDensity(double temp, double pres, double relHumidity) {
        double pSat = saturationVaporPressure(temp - 273.15);
        double pV = relHumidity * pSat;
        double pD = pres - pV;
        return new Result((pD / (R_DRY_AIR * temp)) + (pV / (R_WATER_VAPOR * temp)), pSat, pV, pD);
    }

    public record Result(
            double density,
            double pSat,
            double pV,
            double pD
    ) {}
}


