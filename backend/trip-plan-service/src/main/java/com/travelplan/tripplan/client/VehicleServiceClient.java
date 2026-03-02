package com.travelplan.tripplan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicle-service")
public interface VehicleServiceClient {

    @GetMapping("/api/vehicles/{id}")
    Object getVehicleById(@PathVariable("id") Long id);
}
