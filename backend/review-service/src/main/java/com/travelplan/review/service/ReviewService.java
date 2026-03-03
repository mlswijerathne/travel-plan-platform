package com.travelplan.review.service;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import com.travelplan.review.dto.*;
import com.travelplan.review.entity.EntityType;
import com.travelplan.review.entity.PendingReview;
import com.travelplan.review.entity.Review;
import com.travelplan.review.entity.ReviewProviderResponse;
import com.travelplan.review.mapper.ReviewMapper;
import com.travelplan.review.messaging.RatingUpdatePublisher;
import com.travelplan.review.repository.PendingReviewRepository;
import com.travelplan.review.repository.ReviewProviderResponseRepository;
import com.travelplan.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for all review-related business logic.
 *
 * <p>All write operations run in a transaction.  Read operations use
 * {@code @Transactional(readOnly = true)} to allow JPA read-only optimisations.
 *
 * <p>After every write that changes a provider's aggregate rating a
 * {@code review.rating.updated} SQS event is published so provider services
 * can sync their denormalized ratings.  SQS failures are logged but never
 * roll back the database transaction — the DB is always the source of truth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    /* Maximum number of images allowed per review */
    private static final int MAX_IMAGES = 5;

    private final ReviewRepository                  reviewRepository;
    private final ReviewProviderResponseRepository  responseRepository;
    private final PendingReviewRepository           pendingReviewRepository;
    private final ReviewMapper                      reviewMapper;
    private final RatingUpdatePublisher             ratingUpdatePublisher;

    // ── Tourist: create / update / delete ────────────────────────────────

    /**
     * Creates a new review for a hotel, tour guide, or vehicle.
     *
     * <p>Business rules:
     * <ul>
     *   <li>One review per tourist / entity / booking (DB unique constraint + pre-check)</li>
     *   <li>Maximum {@value MAX_IMAGES} images</li>
     *   <li>A review with a {@code bookingId} is automatically marked as verified</li>
     *   <li>Marks the matching pending-review task as completed after saving</li>
     *   <li>Publishes a {@code review.rating.updated} SQS event</li>
     * </ul>
     *
     * @param touristId Supabase Auth user-ID extracted from the JWT
     * @param request   validated request body
     * @return the persisted review as a DTO
     */
    @Transactional
    public ReviewDto createReview(String touristId, CreateReviewRequest request) {
        log.info("Tourist {} creating review for {} id={}",
                touristId, request.getEntityType(), request.getEntityId());

        // Prevent duplicate reviews for the same booking / entity combination
        if (request.getBookingId() != null
                && reviewRepository.existsByTouristIdAndEntityTypeAndEntityIdAndBookingId(
                        touristId, request.getEntityType(),
                        request.getEntityId(), request.getBookingId())) {
            throw new ValidationException(
                    "You have already reviewed this provider for booking #" + request.getBookingId());
        }
        // Prevent duplicate no-booking reviews for the same entity
        if (request.getBookingId() == null
                && reviewRepository.existsByTouristIdAndEntityTypeAndEntityId(
                        touristId, request.getEntityType(), request.getEntityId())) {
            throw new ValidationException("You have already reviewed this provider");
        }

        // Validate image count
        if (request.getImages() != null && request.getImages().size() > MAX_IMAGES) {
            throw new ValidationException("A review can contain at most " + MAX_IMAGES + " images");
        }

        Review review = Review.builder()
                .touristId(touristId)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages() != null
                        ? new java.util.ArrayList<>(request.getImages())
                        : new java.util.ArrayList<>())
                .isVerified(request.getBookingId() != null)
                .isVisible(true)
                .build();

        review = reviewRepository.save(review);
        log.info("Review {} saved for tourist {}", review.getId(), touristId);

        markPendingReviewCompleted(touristId, request.getEntityType(),
                request.getEntityId(), request.getBookingId());

        publishRatingUpdateEvent(request.getEntityType(), request.getEntityId());

        return reviewMapper.toDto(review);
    }

    /**
     * Updates the rating, title, content, or images of an existing review.
     * Only the review's original author may update it.
     * Fields supplied as {@code null} are ignored (partial update).
     *
     * @param reviewId  primary-key of the review
     * @param touristId Supabase user-ID of the caller (ownership check)
     * @param request   fields to update
     * @return the updated review as a DTO
     */
    @Transactional
    public ReviewDto updateReview(Long reviewId, String touristId, UpdateReviewRequest request) {
        log.info("Tourist {} updating review {}", touristId, reviewId);

        Review review = findOwnedReview(reviewId, touristId);

        if (request.getRating()  != null) review.setRating(request.getRating());
        if (request.getTitle()   != null) review.setTitle(request.getTitle());
        if (request.getContent() != null) review.setContent(request.getContent());
        if (request.getImages()  != null) {
            if (request.getImages().size() > MAX_IMAGES) {
                throw new ValidationException("A review can contain at most " + MAX_IMAGES + " images");
            }
            review.setImages(request.getImages());
        }

        review = reviewRepository.save(review);
        publishRatingUpdateEvent(review.getEntityType(), review.getEntityId());
        return reviewMapper.toDto(review);
    }

    /**
     * Soft-deletes a review by setting {@code isVisible = false}.
     * Only the review's author may call this; admins use the visibility endpoint.
     *
     * @param reviewId  primary-key of the review
     * @param touristId Supabase user-ID of the caller
     */
    @Transactional
    public void deleteReview(Long reviewId, String touristId) {
        log.info("Tourist {} deleting review {}", touristId, reviewId);
        Review review = findOwnedReview(reviewId, touristId);
        review.setIsVisible(false);
        reviewRepository.save(review);
        publishRatingUpdateEvent(review.getEntityType(), review.getEntityId());
    }

    // ── Tourist: read ────────────────────────────────────────────────────

    /**
     * Returns all reviews written by the authenticated tourist, newest first.
     *
     * @param touristId Supabase Auth user-ID
     * @param page      0-based page number
     * @param size      page size
     * @return paginated list of the tourist's reviews
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ReviewDto> getMyReviews(String touristId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> result = reviewRepository.findByTouristId(touristId, pageable);
        return PaginatedResponse.of(result.map(reviewMapper::toDto).getContent(), page, size, result.getTotalElements());
    }

    /**
     * Returns outstanding pending review tasks for the tourist — providers
     * from completed trips that have not been reviewed yet.
     *
     * @param touristId Supabase Auth user-ID
     * @param page      0-based page number
     * @param size      page size
     * @return paginated list of pending reviews
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<PendingReviewDto> getPendingReviews(String touristId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PendingReview> result =
                pendingReviewRepository.findByTouristIdAndIsCompletedFalseOrderByCreatedAtDesc(touristId, pageable);
        return PaginatedResponse.of(result.map(reviewMapper::toPendingDto).getContent(), page, size, result.getTotalElements());
    }

    // ── Public / provider ────────────────────────────────────────────────

    /**
     * Returns visible reviews for a specific provider entity.
     *
     * @param entityType HOTEL, TOUR_GUIDE, or VEHICLE
     * @param entityId   primary-key of the provider
     * @param page       0-based page number
     * @param size       page size
     * @return paginated list of reviews
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ReviewDto> getReviewsByEntity(EntityType entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> result = reviewRepository.findByEntityTypeAndEntityIdAndIsVisibleTrue(entityType, entityId, pageable);
        return PaginatedResponse.of(result.map(reviewMapper::toDto).getContent(), page, size, result.getTotalElements());
    }

    /**
     * Fetches a single review by ID, eager-loading its provider responses.
     *
     * @param reviewId primary-key of the review
     * @return the full review DTO including responses
     */
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findByIdWithResponses(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return reviewMapper.toDto(review);
    }

    /**
     * Returns the aggregate rating summary (average, total count, and star
     * distribution histogram) for a provider entity.
     *
     * <p>Returns zeroed values — not null — when no reviews exist.
     *
     * @param entityType HOTEL, TOUR_GUIDE, or VEHICLE
     * @param entityId   primary-key of the provider
     * @return rating summary DTO
     */
    @Transactional(readOnly = true)
    public ReviewSummaryDto getReviewSummary(EntityType entityType, Long entityId) {
        long totalCount = reviewRepository.countByEntityTypeAndEntityIdAndIsVisibleTrue(entityType, entityId);

        Double avgRaw = reviewRepository.findAverageRating(entityType, entityId);
        BigDecimal average = (avgRaw == null)
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(avgRaw).setScale(2, RoundingMode.HALF_UP);

        // Build distribution map from raw query result
        Map<Integer, Integer> dist = new HashMap<>();
        for (Object[] row : reviewRepository.countRatingDistribution(entityType, entityId)) {
            dist.put((Integer) row[0], ((Long) row[1]).intValue());
        }

        return ReviewSummaryDto.builder()
                .entityType(entityType)
                .entityId(entityId)
                .averageRating(average)
                .reviewCount((int) totalCount)
                .fiveStarCount(dist.getOrDefault(5, 0))
                .fourStarCount(dist.getOrDefault(4, 0))
                .threeStarCount(dist.getOrDefault(3, 0))
                .twoStarCount(dist.getOrDefault(2, 0))
                .oneStarCount(dist.getOrDefault(1, 0))
                .build();
    }

    /**
     * Allows a provider to post a public reply to a tourist review.
     * Only one response per provider per review is permitted.
     *
     * @param reviewId   primary-key of the review
     * @param providerId Supabase Auth user-ID of the responding provider
     * @param request    the response content
     * @return the created response as a DTO
     */
    @Transactional
    public ProviderResponseDto addProviderResponse(Long reviewId, String providerId, ProviderResponseRequest request) {
        log.info("Provider {} adding response to review {}", providerId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (responseRepository.existsByReviewIdAndProviderId(reviewId, providerId)) {
            throw new ValidationException("You have already responded to review #" + reviewId);
        }

        ReviewProviderResponse response = ReviewProviderResponse.builder()
                .review(review)
                .providerId(providerId)
                .content(request.getContent())
                .build();

        response = responseRepository.save(response);
        return reviewMapper.toResponseDto(response);
    }

    /**
     * Deletes a provider's response.
     * Only the response author or an admin may perform this action.
     *
     * @param reviewId   primary-key of the review (for path validation)
     * @param responseId primary-key of the response
     * @param callerId   Supabase user-ID of the caller
     * @param isAdmin    {@code true} when the caller has ADMIN role
     */
    @Transactional
    public void deleteProviderResponse(Long reviewId, Long responseId, String callerId, boolean isAdmin) {
        ReviewProviderResponse response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response", "id", responseId));

        if (!isAdmin && !response.getProviderId().equals(callerId)) {
            throw new ForbiddenException("You are not allowed to delete this response");
        }

        responseRepository.delete(response);
        log.info("Response {} deleted by caller {}", responseId, callerId);
    }

    // ── Admin ────────────────────────────────────────────────────────────

    /**
     * Toggles (shows / hides) a review without physically deleting it.
     *
     * @param reviewId  primary-key of the review
     * @param isVisible new visibility flag
     * @return the updated review as a DTO
     */
    @Transactional
    public ReviewDto setReviewVisibility(Long reviewId, boolean isVisible) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        review.setIsVisible(isVisible);
        review = reviewRepository.save(review);
        log.info("Review {} visibility set to {} by admin", reviewId, isVisible);

        publishRatingUpdateEvent(review.getEntityType(), review.getEntityId());
        return reviewMapper.toDto(review);
    }

    // ── Internal (SQS consumer) ──────────────────────────────────────────

    /**
     * Creates {@link PendingReview} records for each booking item in a
     * completed trip.  Called by the SQS message listener when an
     * {@code itinerary.trip.completed} event is received.
     *
     * <p>Processing is idempotent: duplicate events for the same
     * tourist / entity / booking are silently skipped.
     *
     * @param event deserialized trip-completion event from SQS
     */
    @Transactional
    public void processTripCompletionEvent(TripCompletionEvent event) {
        if (event.getPayload() == null || event.getPayload().getBookingItems() == null) {
            log.warn("Received TripCompletionEvent with empty payload — skipping");
            return;
        }

        String touristId = event.getPayload().getTouristId();
        List<TripCompletionEvent.BookingItem> items = event.getPayload().getBookingItems();
        log.info("Processing trip completion for tourist {} — {} item(s)", touristId, items.size());

        for (TripCompletionEvent.BookingItem item : items) {
            // Skip duplicates (idempotent SQS processing)
            if (pendingReviewRepository.existsByTouristIdAndEntityTypeAndEntityIdAndBookingId(
                    touristId, item.getEntityType(), item.getEntityId(), item.getBookingId())) {
                continue;
            }

            pendingReviewRepository.save(PendingReview.builder()
                    .touristId(touristId)
                    .entityType(item.getEntityType())
                    .entityId(item.getEntityId())
                    .entityName(item.getEntityName())
                    .bookingId(item.getBookingId())
                    .tripEndDate(event.getPayload().getTripEndDate())
                    .isCompleted(false)
                    .build());

            log.info("Pending review created: tourist={} entity={} booking={}",
                    touristId, item.getEntityId(), item.getBookingId());
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    /**
     * Loads a review by ID and verifies it belongs to the given tourist.
     *
     * @throws ResourceNotFoundException if the review does not exist
     * @throws ForbiddenException        if the tourist does not own the review
     */
    private Review findOwnedReview(Long reviewId, String touristId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        if (!review.getTouristId().equals(touristId)) {
            throw new ForbiddenException("You are not allowed to modify review #" + reviewId);
        }
        return review;
    }

    /**
     * Marks an outstanding pending-review task as completed once the tourist
     * submits the actual review.  Does nothing if no matching task exists.
     */
    private void markPendingReviewCompleted(String touristId, EntityType entityType,
                                             Long entityId, Long bookingId) {
        if (bookingId == null) return;

        pendingReviewRepository
                .findByTouristIdAndEntityTypeAndEntityIdAndBookingId(touristId, entityType, entityId, bookingId)
                .ifPresent(pending -> {
                    pending.setIsCompleted(true);
                    pendingReviewRepository.save(pending);
                });
    }

    /**
     * Recalculates the aggregate rating for a provider and publishes a
     * {@code review.rating.updated} SQS event.
     * Errors are logged without rolling back the caller's transaction.
     */
    private void publishRatingUpdateEvent(EntityType entityType, Long entityId) {
        try {
            long count = reviewRepository.countByEntityTypeAndEntityIdAndIsVisibleTrue(entityType, entityId);
            Double avgRaw = reviewRepository.findAverageRating(entityType, entityId);
            BigDecimal avg = (avgRaw == null)
                    ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(avgRaw).setScale(2, RoundingMode.HALF_UP);

            ratingUpdatePublisher.publish(entityType.name(), entityId, avg, (int) count);
        } catch (Exception e) {
            log.error("Failed to publish rating update for {} id={}: {}",
                    entityType, entityId, e.getMessage());
        }
    }
}
