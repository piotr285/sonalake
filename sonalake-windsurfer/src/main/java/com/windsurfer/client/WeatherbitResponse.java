package com.windsurfer.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class WeatherbitResponse {

    private WeatherbitResponse() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastResponse(
            @JsonProperty("data") List<DayForecast> data,
            @JsonProperty("city_name") String cityName,
            @JsonProperty("country_code") String countryCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DayForecast(
            @JsonProperty("datetime") String datetime,     // "YYYY-MM-DD"
            @JsonProperty("temp") double temp,         // avg temp °C
            @JsonProperty("wind_spd") double windSpd) {
    }
}
