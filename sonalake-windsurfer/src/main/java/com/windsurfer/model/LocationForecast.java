package com.windsurfer.model;

public record LocationForecast(Location location, WeatherConditions weather, Double windsurfScore) {
}
