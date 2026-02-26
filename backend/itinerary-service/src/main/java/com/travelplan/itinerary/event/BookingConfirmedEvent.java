package com.travelplan.itinerary.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Event published by BookingService when a booking is confirmed.
 * Uses envelope pattern for consistency across services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingConfirmedEvent {
    private String eventType; // booking.confirmed
    private String eventId;
    private Instant timestamp;
    private String version;
    private String source; // booking-service
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private Long bookingId;
        private String bookingReference;
        private String touristId;
        private List<BookingItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookingItem {
        private String providerType; // HOTEL, TRANSPORT, ACTIVITY, GUIDE
        private Long providerId;
        private String itemName;
        private String startDate; // ISO format
        private String endDate;   // ISO format
        private String status;
    }
}
