package com.travelplan.ecommerce.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Uses Eureka service discovery to reach the Hotel Service
@FeignClient(name = "hotel-service")
public interface HotelServiceClient {
    
    @GetMapping("/api/hotels/{id}")
    Object verifyHotelExists(@PathVariable("id") String id);
}