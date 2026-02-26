package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hotel-service")
public interface HotelServiceClient {

    @GetMapping("/api/hotels")
    ApiResponse<Object> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/hotels/{id}")
    ApiResponse<Object> getHotelById(@PathVariable String id);

    @GetMapping("/api/hotels/search")
    ApiResponse<Object> searchHotelsByQuery(@RequestParam Map<String, String> params);
}
