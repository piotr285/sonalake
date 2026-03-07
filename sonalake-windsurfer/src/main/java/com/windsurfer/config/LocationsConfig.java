package com.windsurfer.config;

import com.windsurfer.model.Location;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LocationsConfig {

    @Bean
    public List<Location> windsurfingLocations() {
        return List.of(
                new Location("jastarnia",  "Jastarnia",  "Poland",    54.6961,  18.6786),
                new Location("bridgetown", "Bridgetown", "Barbados",  13.0975, -59.6167),
                new Location("fortaleza",  "Fortaleza",  "Brazil",    -3.7172, -38.5433),
                new Location("pissouri",   "Pissouri",   "Cyprus",    34.6693,  32.7007),
                new Location("le_morne",   "Le Morne",   "Mauritius", -20.4500,  57.3167)
        );
    }
}
