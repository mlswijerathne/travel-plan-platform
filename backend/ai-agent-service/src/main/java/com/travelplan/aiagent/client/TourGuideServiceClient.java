package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tour-guide-service", url = "${feign.tour-guide-service.url:}")
public interface TourGuideServiceClient {

    @GetMapping("/api/tour-guides")
    Object searchTourGuides(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/tour-guides/{id}")
    ApiResponse<Object> getTourGuideById(@PathVariable String id);
}
