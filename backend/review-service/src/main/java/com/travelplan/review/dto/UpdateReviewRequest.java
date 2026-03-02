package com.travelplan.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Request body for {@code PUT /api/reviews/{id}}.
 *
 * <p>Only the tourist who owns the review can update it.
 * The {@code entityType}, {@code entityId}, and {@code bookingId}
 * fields are immutable after creation — only the review content itself
 * (rating, title, content, images) can be changed.
 */
@Data
public class UpdateReviewRequest {

    /**
     * Updated star rating (1–5).  If omitted, the existing rating is kept.
     */
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    /** Updated headline — pass {@code null} to clear the existing title */
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /** Updated review text */
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    /**
     * Replacement image list.  Passing an empty list removes all images.
     * Passing {@code null} leaves the existing image list unchanged.
     */
    private List<String> images;
}
