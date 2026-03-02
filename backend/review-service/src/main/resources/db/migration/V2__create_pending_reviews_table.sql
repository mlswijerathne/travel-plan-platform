-- =============================================================================
-- V2: Create pending_reviews table
-- =============================================================================
-- Purpose:
--   When a tourist completes a trip the Itinerary Service publishes an SQS
--   event. The Review Service consumes it and creates one pending_review row
--   per bookable item so the tourist can see exactly which providers they
--   still need to review (FR44).
--
-- Design decisions:
--   * Rows are never deleted after review submission — is_completed is set to
--     TRUE instead.  This preserves creation timestamps for analytics.
--   * A unique constraint prevents duplicate rows when SQS messages are
--     redelivered (idempotent processing).
-- =============================================================================

CREATE TABLE pending_reviews
(
    id            BIGSERIAL PRIMARY KEY,

    -- Supabase Auth user-ID of the tourist who should write the review
    tourist_id    VARCHAR(255)             NOT NULL,

    -- Provider type: HOTEL, TOUR_GUIDE, VEHICLE
    entity_type   VARCHAR(50)              NOT NULL,

    -- Primary-key of the provider in its own service's database
    entity_id     BIGINT                   NOT NULL,

    -- Human-readable provider name (denormalized for fast list rendering)
    entity_name   VARCHAR(255),

    -- Booking that unlocked this review request
    booking_id    BIGINT                   NOT NULL,

    -- Date the trip ended (for display and sorting)
    trip_end_date DATE,

    -- FALSE while review is outstanding; set to TRUE after submission
    is_completed  BOOLEAN                  NOT NULL DEFAULT FALSE,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Idempotency: one pending-review record per tourist / booking / entity
    CONSTRAINT pending_reviews_uk_tourist_booking_entity
        UNIQUE (tourist_id, booking_id, entity_type, entity_id)
);

-- Index to quickly find all outstanding review tasks for a tourist
CREATE INDEX idx_pending_reviews_tourist_id
    ON pending_reviews (tourist_id);

-- Partial index: only include rows that still need a review (common query)
CREATE INDEX idx_pending_reviews_outstanding
    ON pending_reviews (tourist_id, created_at DESC)
    WHERE is_completed = FALSE;
