package com.travelplan.review.dto;

import com.travelplan.review.entity.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read-only view of a pending review task for a tourist.
 *
 * <p>Returned by {@code GET /api/reviews/pending}.  Each item represents
 * a provider that the tourist booked but has not reviewed yet.
 */
@Data
@Builder
public class PendingReviewDto {

    /** Unique identifier of the pending-review record */
    private Long id;

    /** Supabase Auth user-ID of the tourist */
    private String touristId;

    /** Provider type: HOTEL, TOUR_GUIDE, or VEHICLE */
    private EntityType entityType;

    /** Primary-key of the provider in its own service's database */
    private Long entityId;

    /**
     * Human-readable name of the provider (e.g. "Sunset Beach Hotel").
     * Denormalized so the UI can render the list without extra API calls.
     */
    private String entityName;

    /** Booking that created this pending review request */
    private Long bookingId;

    /** Date when the trip using this provider ended */
    private LocalDate tripEndDate;

    /**
     * {@code false} = review not yet written (should be shown in the UI).
     * {@code true}  = review already submitted.
     */
    private Boolean isCompleted;

    /** ISO-8601 timestamp of when this pending record was created */
    private Instant createdAt;
}
