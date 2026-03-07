package com.windsurfer.controller;

import com.windsurfer.model.BestSpotResponse;
import com.windsurfer.model.Location;
import com.windsurfer.service.BestSpotService;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class WindsurferController {
    private final BestSpotService bestSpotService;
    private final List<Location> locations;

    public WindsurferController(BestSpotService bestSpotService, List<Location> locations) {
        this.bestSpotService = bestSpotService;
        this.locations = locations;
    }

    @GetMapping("/best-spot")
    public ResponseEntity<BestSpotResponse> bestSpot(
            @RequestParam("date") @NotNull(message = "date is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bestSpotService.findBestSpot(date));
    }

    @GetMapping("/locations")
    public ResponseEntity<Map<String, List<Location>>> locations() {
        return ResponseEntity.ok(Map.of("locations", locations));
    }
}
