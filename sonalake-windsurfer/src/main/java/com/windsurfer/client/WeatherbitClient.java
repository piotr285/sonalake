package com.windsurfer.client;

import com.windsurfer.config.WeatherbitProperties;
import com.windsurfer.exception.WeatherServiceException;
import com.windsurfer.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
public class WeatherbitClient {
    private static final Logger log = LoggerFactory.getLogger(WeatherbitClient.class);
    private final RestTemplate restTemplate;
    private final WeatherbitProperties props;

    public WeatherbitClient(RestTemplate restTemplate, WeatherbitProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    @Cacheable(value = "forecasts", key = "#location.id")
    public List<WeatherbitResponse.DayForecast> fetchDailyForecast(Location location) {
        String url = UriComponentsBuilder.newInstance()
                .uri(URI.create(props.getBaseUrl()))
                .path("/forecast/daily")
                .queryParam("lat", location.getLatitude())
                .queryParam("lon", location.getLongitude())
                .queryParam("key", props.getKey())
                .queryParam("days", props.getForecastDays())
                .queryParam("units", "M")
                .toUriString();
        log.info("Fetching forecast for {} ({}, {})", location.getName(), location.getLatitude(), location.getLongitude());
        try {
            var response = restTemplate.getForObject(url, WeatherbitResponse.ForecastResponse.class);
            if (response == null || response.data() == null) {
                throw new WeatherServiceException("Empty response from Weatherbit for location: " + location.getName());
            }
            return response.data();
        } catch (HttpClientErrorException.Forbidden e) {
            throw new WeatherServiceException("Invalid Weatherbit API key", e);
        } catch (RestClientException e) {
            throw new WeatherServiceException("Failed to fetch forecast for " + location.getName() + ": " + e.getMessage(), e);
        }
    }
}
