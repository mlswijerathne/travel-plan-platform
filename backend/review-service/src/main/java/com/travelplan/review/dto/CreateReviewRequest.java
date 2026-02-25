package com.travelplan.review.dto;

import com.travelplan.review.entity.EntityType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Request body for {@code POST /api/reviews}.
 *
 * <p>A tourist submits this DTO to create a new review for a hotel,
 * tour guide, or vehicle.  All fields that affect the database
 * integrity constraint are required.
 */
@Data
public class CreateReviewRequest {

    /**
     * Type of provider being reviewed (HOTEL, TOUR_GUIDE, VEHICLE).
     * Must match the {@link EntityType} enum — if invalid, Jakarta
     * Validation returns a 400 response via the global exception handler.
     */
    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    /** Primary-key of the provider in its own service's database */
    @NotNull(message = "Entity ID is required")
    @Positive(message = "Entity ID must be a positive number")
    private Long entityId;

    /**
     * Booking ID that proves the tourist actually used this provider.
     * When present, the review will be marked as {@code isVerified = true}.
     */
    private Long bookingId;

    /**
     * Star rating between 1 (worst) and 5 (best).
     * Validated at both the DTO and the database layer.
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    /** Optional headline that summarises the experience */
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /** Full review text */
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    /**
     * Optional list of image URLs or storage paths (e.g. Supabase Storage
     * public URLs) attached to this review.  Maximum 5 images enforced at
     * the service layer.
     */
    private List<String> images;
}
