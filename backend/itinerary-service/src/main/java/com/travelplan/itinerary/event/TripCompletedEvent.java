package com.travelplan.itinerary.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Event published by ItineraryService when a trip is completed.
 * Sent to ReviewService to trigger review prompts for the tourist.
 * Uses envelope pattern for consistency with other services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripCompletedEvent {
    private String eventType; // itinerary.trip.completed
    private String eventId;
    private Instant timestamp;
    private String version;
    private String source; // itinerary-service
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private String touristId;
        private Long itineraryId;
        private LocalDate tripEndDate;
        private List<BookingItem> bookingItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookingItem {
        private String entityType; // HOTEL, TOUR_GUIDE, ACTIVITY, VEHICLE
        private Long entityId; // provider ID
        private String entityName; // provider name
        private Long bookingId; // booking reference
    }

    /**
     * Factory method to create a TripCompletedEvent with proper envelope
     */
    public static TripCompletedEvent of(String touristId, Long itineraryId, LocalDate tripEndDate, List<BookingItem> bookingItems) {
        return TripCompletedEvent.builder()
                .eventType("itinerary.trip.completed")
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .version("1.0")
                .source("itinerary-service")
                .payload(Payload.builder()
                        .touristId(touristId)
                        .itineraryId(itineraryId)
                        .tripEndDate(tripEndDate)
                        .bookingItems(bookingItems)
                        .build())
                .build();
    }
}
