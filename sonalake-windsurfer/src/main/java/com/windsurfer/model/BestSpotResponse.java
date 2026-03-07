package com.windsurfer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record BestSpotResponse(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
                               @JsonProperty("best_location") BestLocation bestLocation,
                               @JsonProperty("all_locations") List<LocationResult> allLocations) {

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

    public record LocationResult(String location, String country, double score, WeatherConditions weather) {
        public static BestSpotResponse.LocationResult toLocationResult(LocationForecast lf) {
            return new BestSpotResponse.LocationResult(
                    lf.location().name(),
                    lf.location().country(),
                    lf.windsurfScore(),
                    lf.weather()
            );
        }
    }
}
