package com.travelplan.review.repository;

import com.travelplan.review.entity.EntityType;
import com.travelplan.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link Review} aggregate.
 *
 * <p>All query method names follow Spring Data naming conventions so the
 * framework generates SQL automatically.  Custom JPQL/native queries are
 * annotated with {@code @Query} to keep the intent explicit.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Retrieves a paginated list of visible reviews for a given provider.
     *
     * @param entityType the type of provider (HOTEL, TOUR_GUIDE, VEHICLE)
     * @param entityId   primary-key of the provider in its own service
     * @param pageable   pagination and sorting parameters
     * @return page of matching reviews
     */
    Page<Review> findByEntityTypeAndEntityIdAndIsVisibleTrue(
            EntityType entityType, Long entityId, Pageable pageable);

    /**
     * Retrieves all reviews written by a specific tourist, newest first.
     *
     * @param touristId Supabase Auth user-ID of the tourist
     * @param pageable  pagination parameters
     * @return page of the tourist's reviews
     */
    Page<Review> findByTouristId(String touristId, Pageable pageable);

    /**
     * Checks whether a tourist has already reviewed a specific provider
     * for a given booking to prevent duplicate reviews.
     *
     * @param touristId  Supabase Auth user-ID
     * @param entityType provider type
     * @param entityId   provider ID
     * @param bookingId  booking ID (nullable in the DB but used here as discriminator)
     * @return {@code true} if a review already exists
     */
    boolean existsByTouristIdAndEntityTypeAndEntityIdAndBookingId(
            String touristId, EntityType entityType, Long entityId, Long bookingId);

    /**
     * Fetches a single review by ID, eagerly loading its provider responses
     * to avoid the N+1 query problem on the detail endpoint.
     *
     * @param reviewId primary-key of the review
     * @return optional review with responses populated
     */
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.responses WHERE r.id = :reviewId")
    Optional<Review> findByIdWithResponses(@Param("reviewId") Long reviewId);

    /**
     * Calculates the average rating for a provider across all visible reviews.
     *
     * @param entityType provider type
     * @param entityId   provider ID
     * @return average rating, or {@code null} if there are no reviews
     */
    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.entityType = :entityType
              AND r.entityId   = :entityId
              AND r.isVisible  = true
            """)
    Double findAverageRating(
            @Param("entityType") EntityType entityType,
            @Param("entityId")   Long entityId);

    /**
     * Counts all visible reviews for a provider.
     *
     * @param entityType provider type
     * @param entityId   provider ID
     * @return total count of visible reviews
     */
    long countByEntityTypeAndEntityIdAndIsVisibleTrue(EntityType entityType, Long entityId);

    /**
     * Counts reviews grouped by star value (1–5) for the rating histogram.
     * Returns a projection array of {@code [rating, count]} pairs.
     *
     * @param entityType provider type
     * @param entityId   provider ID
     * @return list of Object[] where index 0 is rating (Integer) and index 1 is count (Long)
     */
    @Query("""
            SELECT r.rating, COUNT(r)
            FROM Review r
            WHERE r.entityType = :entityType
              AND r.entityId   = :entityId
              AND r.isVisible  = true
            GROUP BY r.rating
            ORDER BY r.rating DESC
            """)
    java.util.List<Object[]> countRatingDistribution(
            @Param("entityType") EntityType entityType,
            @Param("entityId")   Long entityId);
}
