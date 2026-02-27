package com.travelplan.ecommerce.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// This tells E-Commerce how to reach the Hotel Service on Port 8083
@FeignClient(name = "hotel-service", url = "http://localhost:8083")
public interface HotelServiceClient {
    
    @GetMapping("/api/hotels/{id}")
    Object verifyHotelExists(@PathVariable("id") String id);
}