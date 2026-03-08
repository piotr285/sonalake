package com.windsurfer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "weatherbit.api")
public class WeatherbitProperties {
    private String baseUrl = "https://api.weatherbit.io/v2.0";
    private String key;
    private int forecastDays = 7;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;
}