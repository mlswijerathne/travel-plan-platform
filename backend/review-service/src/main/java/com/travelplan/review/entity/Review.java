package com.travelplan.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity that maps to the {@code reviews} table.
 *
 * <p>A review is written by a tourist for a specific provider entity
 * (HOTEL, TOUR_GUIDE, or VEHICLE) after completing a booking.
 * The unique constraint ensures one review per tourist / entity / booking combination.
 */
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "reviews_uk_tourist_entity",
        columnNames = {"tourist_id", "entity_type", "entity_id", "booking_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    /** Auto-incremented surrogate primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Supabase Auth user-ID of the tourist who wrote the review.
     * Not a foreign-key into another service's table — cross-service
     * data is accessed via API calls, never via direct DB join.
     */
    @Column(name = "tourist_id", nullable = false, length = 255)
    private String touristId;

    /**
     * Discriminator identifying which provider type is being reviewed.
     * Stored as a VARCHAR(50) string in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    /** Primary-key ID of the provider record in its own service's database */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Optional reference to the booking that unlocks this review.
     * Kept nullable so that admin-created or externally imported reviews
     * are still supported.
     */
    @Column(name = "booking_id")
    private Long bookingId;

    /**
     * Star rating from 1 (worst) to 5 (best).
     * The CHECK constraint is enforced at the DB level in V1 migration.
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /** Optional short headline for the review */
    @Column(name = "title", length = 255)
    private String title;

    /** Full review text — stored as TEXT in PostgreSQL */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * List of image URLs / storage paths attached to the review.
     * Mapped to a PostgreSQL {@code TEXT[]} column using Hibernate 6's
     * native array JDBC type.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;

    /**
     * {@code true} when the review was made against a confirmed booking,
     * ensuring authenticity.
     */
    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    /**
     * Soft-visibility flag.  Admins can hide a review without deleting it.
     */
    @Builder.Default
    @Column(name = "is_visible")
    private Boolean isVisible = true;

    /** UTC timestamp set on INSERT */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** UTC timestamp refreshed on every UPDATE */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Provider responses associated with this review.
     * Loaded lazily to avoid N+1 queries on listing endpoints.
     */
    @Builder.Default
    @OneToMany(
        mappedBy = "review",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<ReviewProviderResponse> responses = new ArrayList<>();

    // ── Lifecycle callbacks ──────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt  = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
