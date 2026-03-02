package com.travelplan.review.dto;

import com.travelplan.review.entity.EntityType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Aggregate rating summary for a single provider entity.
 *
 * <p>Returned by {@code GET /api/reviews/summary/{entityType}/{entityId}}.
 * This is the data that other services (Hotel, Tour Guide, Vehicle) would
 * consume via Feign clients when they need to display up-to-date ratings.
 */
@Data
@Builder
public class ReviewSummaryDto {

    /** Provider type: HOTEL, TOUR_GUIDE, or VEHICLE */
    private EntityType entityType;

    /** Primary-key of the provider in its own service's database */
    private Long entityId;

    /**
     * Average star rating rounded to two decimal places.
     * Returns {@code 0.00} when no reviews exist.
     */
    private BigDecimal averageRating;

    /** Total number of reviews included in the average */
    private int reviewCount;

    /** Number of reviews with rating == 5 */
    private int fiveStarCount;

    /** Number of reviews with rating == 4 */
    private int fourStarCount;

    /** Number of reviews with rating == 3 */
    private int threeStarCount;

    /** Number of reviews with rating == 2 */
    private int twoStarCount;

    /** Number of reviews with rating == 1 */
    private int oneStarCount;
}
