package com.travelplan.itinerary.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "booking-service", path = "/api/bookings")
public interface BookingServiceClient {

    @PutMapping("/{bookingId}/itinerary/{itineraryId}")
    void linkItinerary(@PathVariable Long bookingId, @PathVariable Long itineraryId);
}
