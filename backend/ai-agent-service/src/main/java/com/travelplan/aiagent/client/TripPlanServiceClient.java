package com.travelplan.aiagent.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "trip-plan-service", url = "${feign.trip-plan-service.url:}")
public interface TripPlanServiceClient {

    @GetMapping("/api/packages")
    Object searchPackages(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/packages/{id}")
    Object getPackageById(@PathVariable String id);
}
