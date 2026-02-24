package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "vehicle-service")
public interface VehicleServiceClient {

    @GetMapping("/api/vehicles")
    ApiResponse<Object> searchVehicles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/vehicles/{id}")
    ApiResponse<Object> getVehicleById(@PathVariable String id);

    @GetMapping("/api/vehicles/search")
    ApiResponse<Object> searchVehiclesByQuery(@RequestParam Map<String, String> params);
}
