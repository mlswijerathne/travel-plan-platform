package com.travelplan.aiagent.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "vehicle-service", url = "${feign.vehicle-service.url:}")
public interface VehicleServiceClient {

    @GetMapping("/api/vehicles")
    Object searchVehicles(
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) BigDecimal minDailyRate,
            @RequestParam(required = false) BigDecimal maxDailyRate,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/vehicles/{id}")
    Object getVehicleById(@PathVariable String id);
}
