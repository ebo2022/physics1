package org.example;

public class AirDensity {

    // gas constants (J/(kg·K))
    private static final double R_DRY_AIR = 287.05;
    private static final double R_WATER_VAPOR = 461.5;

    /**
     * calculate saturation vapor pressure from ambient temperature
     * @param tempCelsius temperature in celsius
     * @return saturation vapor pressure (maximum partial pressure of water vapor air can hold)
     */
    private static double saturationVaporPressure(double tempCelsius) {
        return 610.94 * Math.exp((17.625 * tempCelsius) / (tempCelsius + 243.04));
    }

    /**
     * calculate air density and relevant statistics
     * @param temp temperature in kelvin
     * @param pres pressure in Pa
     * @param relHumidity relative humidity as decima b/w 0 and 1
     * @return record of air density results
     */
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


