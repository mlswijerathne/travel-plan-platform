package com.travelplan.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * JPA entity that maps to the {@code pending_reviews} table.
 *
 * <p>When a trip is completed (detected by the Itinerary Service), an SQS
 * event is published that the Review Service consumes.  For each bookable
 * item in the completed trip, a {@code PendingReview} row is created so
 * the tourist can see exactly which providers still need a review.
 *
 * <p>Once the tourist actually submits a review, {@code isCompleted} is
 * flipped to {@code true} — the row is never deleted so history is preserved.
 */
@Entity
@Table(
    name = "pending_reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "pending_reviews_uk_tourist_booking_entity",
        columnNames = {"tourist_id", "booking_id", "entity_type", "entity_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReview {

    /** Auto-incremented surrogate primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Supabase Auth user-ID of the tourist who should write the review */
    @Column(name = "tourist_id", nullable = false, length = 255)
    private String touristId;

    /** Type of provider entity that must be reviewed */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    /** Primary-key of the provider in its own service's database */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Human-readable name of the provider (e.g. "Sunset Beach Hotel").
     * Denormalized here to avoid a run-time call to the provider service
     * when rendering the pending-review list.
     */
    @Column(name = "entity_name", length = 255)
    private String entityName;

    /** Booking ID that unlocked this review request */
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    /** End date of the trip — useful for display and sorting */
    @Column(name = "trip_end_date")
    private LocalDate tripEndDate;

    /**
     * {@code false} while the tourist has not written the review yet,
     * {@code true} once the review is submitted.
     */
    @Builder.Default
    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    /** UTC timestamp when this pending record was created */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // ── Lifecycle callback ───────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
