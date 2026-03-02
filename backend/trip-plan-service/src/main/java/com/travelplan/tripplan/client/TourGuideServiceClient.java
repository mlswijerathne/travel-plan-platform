package com.travelplan.tripplan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tour-guide-service")
public interface TourGuideServiceClient {

    @GetMapping("/api/tour-guides/{id}")
    Object getTourGuideById(@PathVariable("id") Long id);
}
