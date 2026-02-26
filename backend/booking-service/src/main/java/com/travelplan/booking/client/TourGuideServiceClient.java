package com.travelplan.booking.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tour-guide-service")
public interface TourGuideServiceClient {

    @GetMapping("/api/tour-guides/{id}")
    ApiResponse<Object> getTourGuideById(@PathVariable("id") Long id);

    @GetMapping("/api/tour-guides/{id}/availability")
    ApiResponse<Object> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
}
