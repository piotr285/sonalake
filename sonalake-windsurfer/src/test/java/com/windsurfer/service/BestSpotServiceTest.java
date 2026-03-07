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

    List<Location> locations = new LocationsConfig().windsurfingLocations();

    @BeforeEach
    void setUp() {
        props = new WeatherbitProperties();
        service = new BestSpotService(locations, weatherbitClient, props);
    }

    private WeatherConditions cond(double wind, double temp) {
        return new WeatherConditions(temp, wind);
    }

    @Test
    void returnsHighestScoringLocation() {
        LocalDate date = LocalDate.now().plusDays(1);
        for (Location loc : locations) {
            double wind = loc.id().equals("le_morne") ? 15.0 : 2.0;
            List<String> uniqueList = List.of(loc.id());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.of(cond(wind, 25)));
        }
        var response = service.findBestSpot(date);
        assertThat(response.bestLocation().name()).isEqualTo("Le Morne");
    }

    @Test
    void allLocationsSortedDescending() {
        LocalDate date = LocalDate.now().plusDays(1);
        double[] winds = {2, 5, 8, 12, 15};
        int i = 0;
        for (Location loc : locations) {
            final double w = winds[i++];
            List<String> uniqueList = List.of(loc.id());
            when(weatherbitClient.fetchDailyForecast(loc))
                    .thenReturn((List) uniqueList);
            when(weatherbitClient.extractDay(eq((List) uniqueList), eq(date)))
                    .thenReturn(Optional.of(cond(w, 25)));
        }

        var response = service.findBestSpot(date);
        List<Double> scores = response.allLocations().stream()
                .map(r -> r.score())
                .toList();

        for (int j = 0; j < scores.size() - 1; j++) {
            assertThat(scores.get(j)).isGreaterThanOrEqualTo(scores.get(j + 1));
        }
    }

    @Test
    void responseIncludesWeatherConditions() {
        LocalDate date = LocalDate.now().plusDays(1);
        var expectedWeather = cond(12, 27);

        for (Location loc : locations) {
            List<String> uniqueList = List.of(loc.id());
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
        LocalDate tooFar = LocalDate.now().plusDays(props.forecastDays());
        assertThatThrownBy(() -> service.findBestSpot(tooFar))
                .isInstanceOf(IllegalArgumentException.class);
    }
}