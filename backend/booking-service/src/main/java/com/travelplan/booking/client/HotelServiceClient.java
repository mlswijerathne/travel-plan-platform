package com.travelplan.booking.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hotel-service", url = "${services.hotel-url}")
public interface HotelServiceClient {

    @GetMapping("/api/hotels/{id}")
    ApiResponse<Object> getHotelById(@PathVariable("id") Long id);

    @GetMapping("/api/hotels/{id}/availability")
    ApiResponse<Object> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
}
