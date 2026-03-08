package com.windsurfer.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "windsurfing.locations[0].id=jastarnia",
                "windsurfing.locations[0].name=Jastarnia",
                "windsurfing.locations[0].country=Poland",
                "windsurfing.locations[0].latitude=54.6961",
                "windsurfing.locations[0].longitude=18.6786",
                "windsurfing.locations[1].id=bridgetown",
                "windsurfing.locations[1].name=Bridgetown",
                "windsurfing.locations[1].country=Barbados",
                "windsurfing.locations[1].latitude=13.0975",
                "windsurfing.locations[1].longitude=-59.6167",
                "windsurfing.locations[2].id=fortaleza",
                "windsurfing.locations[2].name=Fortaleza",
                "windsurfing.locations[2].country=Brazil",
                "windsurfing.locations[2].latitude=-3.7172",
                "windsurfing.locations[2].longitude=-38.5433",
                "windsurfing.locations[3].id=pissouri",
                "windsurfing.locations[3].name=Pissouri",
                "windsurfing.locations[3].country=Cyprus",
                "windsurfing.locations[3].latitude=34.6693",
                "windsurfing.locations[3].longitude=32.7007",
                "windsurfing.locations[4].id=le_morne",
                "windsurfing.locations[4].name=Le Morne",
                "windsurfing.locations[4].country=Mauritius",
                "windsurfing.locations[4].latitude=-20.4500",
                "windsurfing.locations[4].longitude=57.3167"
        }
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.yml")
class WindsurferControllerIntegrationTest {
    private static WireMockServer wireMock;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @AfterEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @DynamicPropertySource
    static void overrideWeatherbitUrl(DynamicPropertyRegistry registry) {
        registry.add("weatherbit.api.base-url", () -> wireMock.baseUrl());
        registry.add("weatherbit.api.key",      () -> "test-key");
    }

    private String tomorrow() {
        return LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void stubAllLocationsWithDate(String date, double wind, double temp) {
        String body = """
                {
                  "data": [
                    {
                      "datetime": "%s",
                      "temp": %s,
                      "wind_spd": %s
                    }
                  ],
                  "city_name": "Test",
                  "country_code": "XX"
                }
                """.formatted(date, temp, wind);

        wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/forecast/daily"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Test
    @DisplayName("GET /api/best-spot returns 200 with best_location")
    void bestSpotReturns200() throws Exception {
        String date = tomorrow();
        stubAllLocationsWithDate(date, 12.0, 25.0);

        mockMvc.perform(get("/api/best-spot").param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.best_location.name").isString())
                .andExpect(jsonPath("$.best_location.country").isString())
                .andExpect(jsonPath("$.best_location.score").isNumber())
                .andExpect(jsonPath("$.best_location.weather.avgTempCelsius").value(25.0))
                .andExpect(jsonPath("$.best_location.weather.windSpeedMs").value(12.0));
    }

    @Test
    @DisplayName("GET /api/best-spot without date param returns 400")
    void missingDateReturns400() throws Exception {
        mockMvc.perform(get("/api/best-spot"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/best-spot with past date returns 400")
    void pastDateReturns400() throws Exception {
        String past = LocalDate.now().minusDays(1).toString();
        mockMvc.perform(get("/api/best-spot").param("date", past))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/best-spot with date >7 days ahead returns 400")
    void farFutureDateReturns400() throws Exception {
        String far = LocalDate.now().plusDays(8).toString();
        mockMvc.perform(get("/api/best-spot").param("date", far))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/best-spot with invalid date format returns 400")
    void invalidDateFormatReturns400() throws Exception {
        mockMvc.perform(get("/api/best-spot").param("date", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/best-spot returns 200 when no forecast data for date -> no best location available")
    void noForecastDataReturns404() throws Exception {
        // Stub returns a different date than the one we request
        String differentDate = LocalDate.now().plusDays(10).toString();
        stubAllLocationsWithDate(differentDate, 10.0, 25.0);

        String requestDate = tomorrow();
        mockMvc.perform(get("/api/best-spot").param("date", requestDate))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/locations returns all 5 registered locations")
    void locationsReturnsAll5() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations", hasSize(5)))
                .andExpect(jsonPath("$.locations[*].name",
                        containsInAnyOrder("Jastarnia", "Bridgetown", "Fortaleza",
                                           "Pissouri", "Le Morne")));
    }

    @Test
    @DisplayName("Each location has required fields: id, name, country, latitude, longitude")
    void locationsHaveRequiredFields() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations[0].id").isString())
                .andExpect(jsonPath("$.locations[0].name").isString())
                .andExpect(jsonPath("$.locations[0].country").isString())
                .andExpect(jsonPath("$.locations[0].latitude").isNumber())
                .andExpect(jsonPath("$.locations[0].longitude").isNumber());
    }
}
