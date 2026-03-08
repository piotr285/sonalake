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
    private String baseUrl;
    private String key;
    private int forecastDays;
    private int connectTimeoutMs;
    private int readTimeoutMs;
}