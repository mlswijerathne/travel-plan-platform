package com.travelplan.aiagent.client;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hotel-service", url = "${feign.hotel-service.url:}")
public interface HotelServiceClient {

    @GetMapping("/api/hotels/search")
    Object searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/hotels/{id}")
    ApiResponse<Object> getHotelById(@PathVariable String id);

    @GetMapping("/api/hotels/query")
    Object searchHotelsByQuery(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}
