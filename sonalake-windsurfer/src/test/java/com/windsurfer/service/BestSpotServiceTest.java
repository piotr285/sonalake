package com.windsurfer.service;

import com.windsurfer.client.WeatherbitClient;
import com.windsurfer.config.LocationsConfig;
import com.windsurfer.config.WeatherbitProperties;
import com.windsurfer.model.Location;
import com.windsurfer.model.WeatherConditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BestSpotServiceTest {

    @Mock
    WeatherbitClient weatherbitClient;

    WeatherbitProperties props;
    BestSpotService service;

    List<Location> locations;

    @BeforeEach
    void setUp() {
        props = new WeatherbitProperties();
        props.setForecastDays(7);
        locations = List.of(
                new Location("jastarnia",  "Jastarnia",  "Poland",    54.6961,  18.6786),
                new Location("bridgetown", "Bridgetown", "Barbados",  13.0975, -59.6167),
                new Location("fortaleza",  "Fortaleza",  "Brazil",    -3.7172, -38.5433),
                new Location("pissouri",   "Pissouri",   "Cyprus",    34.6693,  32.7007),
                new Location("le_morne",   "Le Morne",   "Mauritius", -20.4500,  57.3167)
        );

        LocationsConfig locationsConfig = new LocationsConfig();
        locationsConfig.setLocations(locations);
        service = new BestSpotService(locationsConfig, weatherbitClient, props);
    }

    private WeatherConditions cond(double wind, double temp) {
        return new WeatherConditions(temp, wind);
    }

    @Test
    void returnsHighestScoringLocation() {
        LocalDate date = LocalDate.now().plusDays(1);
        for (Location loc : locations) {
            double wind = loc.getId().equals("le_morne") ? 15.0 : 2.0;
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.of(cond(wind, 25)));
        }
        var response = service.findBestSpot(date);
        assertThat(response.bestLocation().name()).isEqualTo("Le Morne");
    }

    @Test
    void responseIncludesWeatherConditions() {
        LocalDate date = LocalDate.now().plusDays(1);
        var expectedWeather = cond(12, 27);

        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.of(expectedWeather));
        }

        var response = service.findBestSpot(date);
        var weather = response.bestLocation().weather();

        assertThat(weather.windSpeedMs()).isEqualTo(12.0);
        assertThat(weather.avgTempCelsius()).isEqualTo(27.0);
    }

    @Test
    void throwsForPastDate() {
        LocalDate past = LocalDate.now().minusDays(1);
        assertThatThrownBy(() -> service.findBestSpot(past))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsForDateTooFarAhead() {
        LocalDate tooFar = LocalDate.now().plusDays(props.getForecastDays());
        assertThatThrownBy(() -> service.findBestSpot(tooFar))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsNoSpotWhenAllLocationsHaveUnsuitableConditions() {
        LocalDate date = LocalDate.now().plusDays(1);
        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            // wind=1.0 — wind is too weak for any location
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.of(cond(1.0, 25)));
        }
        var response = service.findBestSpot(date);
        assertThat(response.bestLocation()).isNull();
    }

    @Test
    void returnsNoSpotWhenNoForecastAvailableForAnyLocation() {
        LocalDate date = LocalDate.now().plusDays(1);
        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            // no forecast for given day
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.empty());
        }
        var response = service.findBestSpot(date);
        assertThat(response.bestLocation()).isNull();
        assertThat(response.date()).isEqualTo(date);
    }

    @Test
    void skipsLocationWhenFetchThrowsException() {
        LocalDate date = LocalDate.now().plusDays(1);
        for (Location loc : locations) {
            if (loc.getId().equals("le_morne")) {
                when(weatherbitClient.fetchDailyForecast(loc))
                        .thenThrow(new RuntimeException("API unavailable"));
            } else {
                List<String> uniqueList = List.of(loc.getId());
                when(weatherbitClient.fetchDailyForecast(loc))
                        .thenReturn((List) uniqueList);
                when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                        .thenReturn(Optional.of(cond(12.0, 25)));
            }
        }
        // le_morne throws exception, but other locations should still be considered
        var response = service.findBestSpot(date);
        assertThat(response.bestLocation()).isNotNull();
        assertThat(response.bestLocation().name()).isNotEqualTo("Le Morne");
    }

    @Test
    void acceptsTodayAsValidDate() {
        LocalDate today = LocalDate.now();
        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(today)))
                    .thenReturn(Optional.of(cond(12.0, 25)));
        }
        var response = service.findBestSpot(today);
        assertThat(response).isNotNull();
        assertThat(response.bestLocation()).isNotNull();
    }

    @Test
    void acceptsLastValidDate() {
        LocalDate lastValid = LocalDate.now().plusDays(props.getForecastDays() - 1);
        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.getId());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(lastValid)))
                    .thenReturn(Optional.of(cond(12.0, 25)));
        }
        var response = service.findBestSpot(lastValid);
        assertThat(response).isNotNull();
    }
}