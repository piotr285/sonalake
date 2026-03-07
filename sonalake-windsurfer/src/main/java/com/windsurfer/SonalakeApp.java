package com.windsurfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SonalakeApp {

    public static void main(String[] args) {
        SpringApplication.run(SonalakeApp.class, args);
    }
}
