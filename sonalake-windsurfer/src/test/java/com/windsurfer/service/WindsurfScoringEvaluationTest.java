package com.windsurfer.service;

import com.windsurfer.model.WeatherConditions;
import com.windsurfer.util.WindsurfScoringEvaluation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WindsurfScoringEvaluationTest {
    private static WeatherConditions cond(double wind, double temp) {
        return new WeatherConditions(temp, wind);
    }

    @Test
    void shouldRoundToOneDecimalPlace() {
        assertEquals(10.1, WindsurfScoringEvaluation.round(10.14));
        assertEquals(10.2, WindsurfScoringEvaluation.round(10.15));
        assertEquals(5.0, WindsurfScoringEvaluation.round(4.96));
    }

    @Test
    void shouldCalculateScoreForOptimalConditions() {
        WeatherConditions conditions = cond(10, 20);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(50.0, score); // 10 * 3 + 20
    }

    @Test
    void shouldReturnZeroWhenWindTooLow() {
        WeatherConditions conditions = cond(4.9, 20);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(WindsurfScoringEvaluation.NOT_SUITABLE_AT_ALL_SCORE, score);
    }

    @Test
    void shouldReturnZeroWhenWindTooHigh() {
        WeatherConditions conditions = cond(18.1, 20);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(WindsurfScoringEvaluation.NOT_SUITABLE_AT_ALL_SCORE, score);
    }

    @Test
    void shouldReturnZeroWhenTemperatureTooLow() {
        WeatherConditions conditions = cond(10, 14.9);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(WindsurfScoringEvaluation.NOT_SUITABLE_AT_ALL_SCORE, score);
    }

    @Test
    void shouldReturnZeroWhenTemperatureTooHigh() {
        WeatherConditions conditions = cond(10, 35.1);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(WindsurfScoringEvaluation.NOT_SUITABLE_AT_ALL_SCORE, score);
    }

    @Test
    void shouldAcceptLowerBoundaryConditions() {
        WeatherConditions conditions = cond(5.0, 15.0);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(30.0, score); // 5*3 + 15
    }

    @Test
    void shouldAcceptUpperBoundaryConditions() {
        WeatherConditions conditions = cond(18.0, 35.0);

        double score = WindsurfScoringEvaluation.optimalConditionsScore(conditions);

        assertEquals(89.0, score); // 18*3 + 35
    }
}