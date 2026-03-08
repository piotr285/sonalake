package com.windsurfer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record BestSpotResponse(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
                               @JsonProperty("best_location") BestLocation bestLocation) {

    public static BestSpotResponse noSpotsFound(LocalDate date) {
        return new BestSpotResponse(date, null);
    }

    public record BestLocation(String name, String country, double score, WeatherConditions weather) {
        public static BestSpotResponse.BestLocation toBestLocation(LocationForecast lf) {
            return new BestSpotResponse.BestLocation(
                    lf.location().name(),
                    lf.location().country(),
                    lf.windsurfScore(),
                    lf.weather()
            );
        }
    }
}
