package com.travelplan.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Read-only view of a single provider response to a review.
 *
 * <p>Returned as a nested list inside {@link ReviewDto#getResponses()}.
 */
@Data
@Builder
public class ProviderResponseDto {

    /** Unique identifier of the response record */
    private Long id;

    /** ID of the review this response belongs to */
    private Long reviewId;

    /** Supabase Auth user-ID of the provider who wrote the response */
    private String providerId;

    /** The response text */
    private String content;

    /** ISO-8601 timestamp of when the response was created */
    private Instant createdAt;
}
