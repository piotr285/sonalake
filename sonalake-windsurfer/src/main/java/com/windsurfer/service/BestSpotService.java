package com.windsurfer.service;

import com.windsurfer.client.WeatherbitClient;
import com.windsurfer.client.WeatherbitResponse;
import com.windsurfer.config.LocationsConfig;
import com.windsurfer.config.WeatherbitProperties;
import com.windsurfer.model.BestSpotResponse;
import com.windsurfer.model.Location;
import com.windsurfer.model.LocationForecast;
import com.windsurfer.util.WindsurfScoringEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BestSpotService {
    private static final Logger log = LoggerFactory.getLogger(BestSpotService.class);
    private final List<Location> locations;
    private final WeatherbitClient weatherbitClient;
    private final WeatherbitProperties props;

    public BestSpotService(List<Location> locations, WeatherbitClient weatherbitClient, WeatherbitProperties props) {
        this.locations = locations;
        this.weatherbitClient = weatherbitClient;
        this.props = props;
    }
//    public BestSpotService(LocationsConfig locationsConfig, WeatherbitClient weatherbitClient, WeatherbitProperties props) {
//        this.locations = locationsConfig.locations();
//        this.weatherbitClient = weatherbitClient;
//        this.props = props;
//    }

    public BestSpotResponse findBestSpot(LocalDate date) {
        validateDateRange(date);
        log.info("Finding best windsurfing spot for {}", date);
        List<LocationForecast> scored = locations.stream()
                .map(loc -> fetchAndScore(loc, date))
                .flatMap(Optional::stream)
                .filter(forecast -> forecast.windsurfScore() > WindsurfScoringEvaluation.NOT_SUITABLE_AT_ALL_SCORE)
                .sorted(Comparator.comparingDouble(LocationForecast::windsurfScore)
                        .reversed().thenComparing(lf -> lf.location().name()))
                .toList();
        Optional<LocationForecast> best = scored.stream().findFirst();
        if (best.isEmpty()) {
            log.info("No suitable windsurfing spot found for {}", date);
            return BestSpotResponse.noSpotsFound(date);
        }
        log.info("Best spot for {}: {} (score={})", date, best.get().location().name(), best.get().windsurfScore());
        return new BestSpotResponse(date, BestSpotResponse.BestLocation.toBestLocation(best.get()));
    }

    private void validateDateRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(props.getForecastDays() - 1);
        if (date.isBefore(today) || date.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    "Date must be between " + today + " and " + maxDate +
                            " (given forecast window). Requested: " + date);
        }
    }

    private Optional<LocationForecast> fetchAndScore(Location loc, LocalDate date) {
        try {
            List<WeatherbitResponse.DayForecast> forecasts = weatherbitClient.fetchDailyForecast(loc);
            return weatherbitClient.extractDay(forecasts, date)
                    .map(weather -> new LocationForecast(loc, weather,
                            WindsurfScoringEvaluation.round(WindsurfScoringEvaluation.optimalConditionsScore(weather))));
        } catch (Exception ex) {
            log.warn("Could not fetch forecast for {}: {}", loc.name(), ex.getMessage());
            return Optional.empty();
        }
    }
}