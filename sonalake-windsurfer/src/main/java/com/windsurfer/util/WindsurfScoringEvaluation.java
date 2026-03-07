package com.windsurfer.util;

import com.windsurfer.model.WeatherConditions;

public class WindsurfScoringEvaluation {
    public static final int NOT_SUITABLE_ATA_ALL_SCORE = 0;
    private static final double WIND_MIN = 5.0;
    private static final double WIND_MAX = 18.0;
    private static final double TEMP_MIN = 15.0;
    private static final double TEMP_MAX = 35.0;
    private static final int WIND_MULTIPLIER = 3;

    public static double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public static double optimalConditionsScore(WeatherConditions conditions) {
        if (suitableConditions(conditions.windSpeedMs(), conditions.avgTempCelsius())) {
            return conditions.windSpeedMs() * WIND_MULTIPLIER + conditions.avgTempCelsius();
        }
        return NOT_SUITABLE_ATA_ALL_SCORE;
    }

    private static boolean suitableConditions(double windSpeed, double temperature) {
        return windSpeed >= WIND_MIN && windSpeed <= WIND_MAX && temperature >= TEMP_MIN && temperature <= TEMP_MAX;
    }
}
