package com.travelplan.itinerary.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "itinerary-service");
        return ResponseEntity.ok(response);
    }
}
