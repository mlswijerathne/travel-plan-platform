package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "tour-guide-service")
public interface TourGuideServiceClient {

    @GetMapping("/api/tour-guides")
    ApiResponse<Object> searchTourGuides(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/tour-guides/{id}")
    ApiResponse<Object> getTourGuideById(@PathVariable String id);

    @GetMapping("/api/tour-guides/search")
    ApiResponse<Object> searchTourGuidesByQuery(@RequestParam Map<String, String> params);
}
