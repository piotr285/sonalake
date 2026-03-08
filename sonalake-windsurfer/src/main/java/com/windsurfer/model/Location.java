package com.windsurfer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private String id;
    private String name;
    private String country;
    private Double latitude;
    private Double longitude;

    public Location() {}

    public Location(String id, String name, String country, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}