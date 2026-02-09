package com.travelplan.booking.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "vehicle-service", url = "${services.vehicle-url}")
public interface VehicleServiceClient {

    @GetMapping("/api/vehicles/{id}")
    ApiResponse<Object> getVehicleById(@PathVariable("id") Long id);

    @GetMapping("/api/vehicles/{id}/availability")
    ApiResponse<Object> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
}
