package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "trip-plan-service")
public interface TripPlanServiceClient {

    @GetMapping("/api/trip-plans/packages")
    ApiResponse<Object> searchPackages(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/trip-plans/packages/{id}")
    ApiResponse<Object> getPackageById(@PathVariable String id);

    @GetMapping("/api/trip-plans/packages/search")
    ApiResponse<Object> searchPackagesByQuery(@RequestParam Map<String, String> params);
}
