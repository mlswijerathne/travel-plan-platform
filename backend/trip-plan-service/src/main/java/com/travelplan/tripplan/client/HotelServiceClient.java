package com.travelplan.tripplan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotel-service")
public interface HotelServiceClient {

    @GetMapping("/api/hotels/{id}")
    Object getHotelById(@PathVariable("id") Long id);
}
