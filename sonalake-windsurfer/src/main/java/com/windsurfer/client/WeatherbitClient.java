package com.windsurfer.client;

import com.windsurfer.config.WeatherbitProperties;
import com.windsurfer.exception.WeatherServiceException;
import com.windsurfer.model.Location;
import com.windsurfer.model.WeatherConditions;
import com.windsurfer.util.WindsurfScoringEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class WeatherbitClient {
    private static final Logger log = LoggerFactory.getLogger(WeatherbitClient.class);
    private final RestTemplate restTemplate;
    private final WeatherbitProperties props;

    public WeatherbitClient(RestTemplate restTemplate, WeatherbitProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    @Cacheable(value = "forecasts", key = "#location.id()")
    public List<WeatherbitResponse.DayForecast> fetchDailyForecast(Location location) {
        String url = UriComponentsBuilder
                .fromUriString(props.getBaseUrl() + "/forecast/daily")
                .queryParam("lat", location.latitude())
                .queryParam("lon", location.longitude())
                .queryParam("key", props.getKey())
                .queryParam("days", props.getForecastDays())
                .queryParam("units", "M")   // metric: m/s, °C
                .toUriString();
        log.info("Fetching forecast for {} ({}, {})",
                location.name(), location.latitude(), location.longitude());
        try {
            var response = restTemplate.getForObject(url, WeatherbitResponse.ForecastResponse.class);
            if (response == null || response.data() == null) {
                throw new WeatherServiceException("Empty response from Weatherbit for location: " + location.name());
            }
            return response.data();
        } catch (HttpClientErrorException.Forbidden e) {
            throw new WeatherServiceException("Invalid Weatherbit API key", e);
        } catch (RestClientException e) {
            throw new WeatherServiceException("Failed to fetch forecast for " + location.name() + ": " + e.getMessage(), e);
        }
    }

    public Optional<WeatherConditions> extractDay(List<WeatherbitResponse.DayForecast> forecasts, LocalDate date) {
        String target = date.toString(); // "YYYY-MM-DD"
        return forecasts.stream()
                .filter(d -> target.equals(d.datetime()))
                .findFirst()
                .map(d -> new WeatherConditions(
                        WindsurfScoringEvaluation.round(d.temp()),
                        WindsurfScoringEvaluation.round(d.windSpd())));
    }
}
