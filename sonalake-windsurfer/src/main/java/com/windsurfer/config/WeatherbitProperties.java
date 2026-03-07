package com.windsurfer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "weatherbit.api")
public class WeatherbitProperties {

    private final String  baseUrl            = "https://api.weatherbit.io/v2.0";
    private String  key; //todo zdobądź klucz!
    private final int     forecastDays       = 7;
    private final int     connectTimeoutMs   = 5000;
    private final int     readTimeoutMs      = 10000;

    public String  getBaseUrl()           { return baseUrl; }
    public String  getKey()               { return key; }
    public int     getForecastDays()      { return forecastDays; }
    public int     getConnectTimeoutMs()      { return connectTimeoutMs; }
    public int     getReadTimeoutMs()      { return readTimeoutMs; }
}
