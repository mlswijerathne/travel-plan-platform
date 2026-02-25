package com.travelplan.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity that maps to the {@code review_responses} table.
 *
 * <p>A provider (hotel owner, tour guide, or vehicle owner) can post a
 * single public reply to a tourist's review.  This is a child record
 * owned by the {@link Review} aggregate root.
 */
@Entity
@Table(name = "review_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewProviderResponse {

    /** Auto-incremented surrogate primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent review.  Uses a LAZY fetch strategy; the response is only
     * loaded when explicitly accessed or when the {@link Review} aggregate
     * is fetched with a JOIN FETCH.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_response_review"))
    private Review review;

    /**
     * Supabase Auth user-ID of the provider who wrote the response.
     * One response per review (enforced at the service layer).
     */
    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    /** The response text — stored as TEXT in PostgreSQL */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** UTC timestamp set on INSERT; never changes */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // ── Lifecycle callback ───────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
