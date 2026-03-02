package com.travelplan.review.repository;

import com.travelplan.review.entity.EntityType;
import com.travelplan.review.entity.PendingReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PendingReview}.
 *
 * <p>Pending review records are created when the Review Service consumes a
 * {@code itinerary.trip.completed} SQS event.  They are marked as completed
 * (not deleted) once the tourist submits a review, preserving audit history.
 */
@Repository
public interface PendingReviewRepository extends JpaRepository<PendingReview, Long> {

    /**
     * Returns all outstanding (not yet completed) pending reviews for a tourist,
     * newest trip first.  Used for the "Write a Review" section in the tourist UI.
     *
     * @param touristId Supabase Auth user-ID of the tourist
     * @param pageable  pagination parameters
     * @return page of incomplete pending review tasks
     */
    Page<PendingReview> findByTouristIdAndIsCompletedFalseOrderByCreatedAtDesc(
            String touristId, Pageable pageable);

    /**
     * Returns ALL pending review records for a tourist (both completed and
     * outstanding), useful for history views.
     *
     * @param touristId Supabase Auth user-ID
     * @param pageable  pagination parameters
     * @return page of all pending review records
     */
    Page<PendingReview> findByTouristId(String touristId, Pageable pageable);

    /**
     * Finds a specific pending review record by tourist, entity, and booking
     * to avoid inserting duplicates when re-processing SQS events.
     *
     * @param touristId  Supabase Auth user-ID
     * @param entityType provider type
     * @param entityId   provider ID
     * @param bookingId  booking ID
     * @return optional pending review record
     */
    Optional<PendingReview> findByTouristIdAndEntityTypeAndEntityIdAndBookingId(
            String touristId, EntityType entityType, Long entityId, Long bookingId);

    /**
     * Checks whether a pending-review entry already exists to support
     * idempotent SQS message processing.
     *
     * @param touristId  Supabase Auth user-ID
     * @param entityType provider type
     * @param entityId   provider ID
     * @param bookingId  booking ID
     * @return {@code true} if the record exists
     */
    boolean existsByTouristIdAndEntityTypeAndEntityIdAndBookingId(
            String touristId, EntityType entityType, Long entityId, Long bookingId);
}
