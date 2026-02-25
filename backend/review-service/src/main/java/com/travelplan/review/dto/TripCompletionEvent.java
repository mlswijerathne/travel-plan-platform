package com.travelplan.review.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.travelplan.review.entity.EntityType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents an SQS event published by the Itinerary Service when a
 * tourist's trip is marked as completed.
 *
 * <p>The Review Service consumes this event to create {@code PendingReview}
 * records — one per bookable item in the trip — so the tourist knows which
 * providers to review after returning home.
 *
 * <p>Unknown JSON fields are silently ignored ({@code @JsonIgnoreProperties})
 * for forward-compatibility; if the Itinerary Service adds new fields in the
 * future, existing consumers will not break.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripCompletionEvent {

    /** Event discriminator, expected value: {@code "itinerary.trip.completed"} */
    private String eventType;

    /** Unique ID of this event instance (UUID) */
    private String eventId;

    /** UTC timestamp of when the event was published */
    private Instant timestamp;

    /** Schema version string (e.g. "1.0") */
    private String version;

    /** Service that published this event */
    private String source;

    /** Type-safe payload wrapper */
    private Payload payload;

    // ── Inner types ────────────────────────────────────────────────────────

    /**
     * Core payload of the trip-completion event.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {

        /** Supabase Auth user-ID of the tourist whose trip has ended */
        private String touristId;

        /** Primary-key of the Itinerary record */
        private Long itineraryId;

        /** The date on which the trip ended */
        private LocalDate tripEndDate;

        /**
         * List of bookable items from the completed trip.
         * Each item represents one provider that the tourist should review.
         */
        private List<BookingItem> bookingItems;
    }

    /**
     * Represents a single bookable item (hotel room, guide slot, or vehicle)
     * from the completed trip.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookingItem {

        /** Provider type: HOTEL, TOUR_GUIDE, or VEHICLE */
        private EntityType entityType;

        /** Primary-key of the provider in its service database */
        private Long entityId;

        /** Human-readable name of the provider (denormalized for display) */
        private String entityName;

        /** Booking ID associated with this provider and trip */
        private Long bookingId;
    }
}
