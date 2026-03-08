package com.windsurfer;

import com.windsurfer.config.LocationsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(LocationsConfig.class)
public class SonalakeApp {
    public static void main(String[] args) {
        SpringApplication.run(SonalakeApp.class, args);
    }
}
