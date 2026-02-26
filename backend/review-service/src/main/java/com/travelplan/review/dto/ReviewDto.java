package com.travelplan.review.dto;

import com.travelplan.review.entity.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Read-only view of a single review, returned to API consumers.
 *
 * <p>This DTO deliberately does not expose the JPA entity to avoid
 * coupling the API contract to the persistence model (an anti-pattern
 * called "entity leakage").
 */
@Data
@Builder
public class ReviewDto {

    /** Unique identifier of the review */
    private Long id;

    /** Supabase Auth user-ID of the tourist who wrote the review */
    private String touristId;

    /** Provider type: HOTEL, TOUR_GUIDE, or VEHICLE */
    private EntityType entityType;

    /** Primary-key of the provider in its own service's database */
    private Long entityId;

    /** Optional reference to the booking that unlocked this review */
    private Long bookingId;

    /** 1–5 star rating */
    private Integer rating;

    /** Optional headline */
    private String title;

    /** Full review text */
    private String content;

    /** List of image URLs / storage paths attached to this review */
    private List<String> images;

    /**
     * {@code true} when the review was made against a confirmed booking —
     * adds a "Verified booking" badge in the UI.
     */
    private Boolean isVerified;

    /** Whether this review is publicly visible */
    private Boolean isVisible;

    /** ISO-8601 timestamp of when the review was created */
    private Instant createdAt;

    /** ISO-8601 timestamp of the last update */
    private Instant updatedAt;

    /** List of provider responses (may be empty) */
    private List<ProviderResponseDto> responses;
}
