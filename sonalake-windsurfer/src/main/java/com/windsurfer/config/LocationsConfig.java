package com.windsurfer.config;

import com.windsurfer.model.Location;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "windsurfing")
@Getter
@Setter
public class LocationsConfig {

    private List<Location> locations;
}