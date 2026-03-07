package com.windsurfer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "weatherbit.api")
public class WeatherbitProperties {

    private String baseUrl = "https://api.weatherbit.io/v2.0";
    private String key;
    private int forecastDays = 7;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;

    public String baseUrl()         { return baseUrl; }
    public String key()             { return key; }
    public int forecastDays()       { return forecastDays; }
    public int connectTimeoutMs()   { return connectTimeoutMs; }
    public int readTimeoutMs()      { return readTimeoutMs; }

    public void setBaseUrl(String v)        { this.baseUrl = v; }
    public void setKey(String v)            { this.key = v; }
    public void setForecastDays(int v)      { this.forecastDays = v; }
    public void setConnectTimeoutMs(int v)  { this.connectTimeoutMs = v; }
    public void setReadTimeoutMs(int v)     { this.readTimeoutMs = v; }
}
