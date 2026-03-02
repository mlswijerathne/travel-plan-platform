package com.travelplan.review.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.review.dto.*;
import com.travelplan.review.entity.EntityType;
import com.travelplan.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes all review-related endpoints under
 * {@code /api/reviews}.
 *
 * <p><b>Security model (enforced at this layer):</b>
 * <ul>
 *   <li>Public endpoints (no JWT required): GET reviews by entity, GET summary, GET single review</li>
 *   <li>TOURIST role: create review, update own review, delete own review,
 *       view own reviews, view pending reviews</li>
 *   <li>Provider roles (HOTEL_OWNER, TOUR_GUIDE, VEHICLE_OWNER):
 *       add response to a review, delete own response</li>
 *   <li>ADMIN role: toggle review visibility, delete any response</li>
 * </ul>
 *
 * <p>The authenticated user's Supabase user-ID is extracted from
 * {@link Authentication#getPrincipal()} which is populated by
 * {@code JwtValidationFilter} in the common-lib.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Submit and manage provider reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    // ── Tourist: create / update / delete own reviews ─────────────────────

    /**
     * Creates a new review for a hotel, tour guide, or vehicle.
     *
     * <p><b>POST /api/reviews</b>
     *
     * @param request    validated review body
     * @param auth       Spring Security authentication (holds tourist user-ID)
     * @return 201 Created with the persisted review
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Submit a review",
               description = "Tourist submits a 1–5 star review for a completed booking")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication auth) {

        String touristId = extractUserId(auth);
        log.info("POST /api/reviews — tourist={} entity={} id={}",
                touristId, request.getEntityType(), request.getEntityId());

        ReviewDto created = reviewService.createReview(touristId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    /**
     * Updates an existing review owned by the authenticated tourist.
     *
     * <p><b>PUT /api/reviews/{id}</b>
     *
     * @param reviewId primary-key of the review to update
     * @param request  fields to update (null fields are ignored)
     * @param auth     authentication token
     * @return 200 OK with the updated review
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Update a review", description = "Tourist updates their own review")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication auth) {

        String touristId = extractUserId(auth);
        log.info("PUT /api/reviews/{} — tourist={}", reviewId, touristId);

        ReviewDto updated = reviewService.updateReview(reviewId, touristId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * Soft-deletes (hides) a review owned by the authenticated tourist.
     *
     * <p><b>DELETE /api/reviews/{id}</b>
     *
     * @param reviewId primary-key of the review
     * @param auth     authentication token
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Delete a review", description = "Tourist hides their own review")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("id") Long reviewId,
            Authentication auth) {

        String touristId = extractUserId(auth);
        log.info("DELETE /api/reviews/{} — tourist={}", reviewId, touristId);

        reviewService.deleteReview(reviewId, touristId);
        return ResponseEntity.noContent().build();
    }

    // ── Tourist: read own reviews / pending reviews ───────────────────────

    /**
     * Returns all reviews written by the authenticated tourist.
     *
     * <p><b>GET /api/reviews/my</b>
     *
     * @param page pagination page (0-based, default 0)
     * @param size page size (default 10)
     * @param auth authentication token
     * @return paginated list of the tourist's reviews
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Get my reviews", description = "Returns all reviews written by the current tourist")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReviewDto>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String touristId = extractUserId(auth);
        PaginatedResponse<ReviewDto> result = reviewService.getMyReviews(touristId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Returns the tourist's pending review queue — providers from completed
     * trips that still need a review.
     *
     * <p><b>GET /api/reviews/pending</b>
     *
     * @param page pagination page (0-based, default 0)
     * @param size page size (default 10)
     * @param auth authentication token
     * @return paginated list of outstanding pending reviews
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('TOURIST')")
    @Operation(summary = "Get pending reviews",
               description = "Returns providers from completed trips that the tourist has not reviewed yet")
    public ResponseEntity<ApiResponse<PaginatedResponse<PendingReviewDto>>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String touristId = extractUserId(auth);
        PaginatedResponse<PendingReviewDto> result =
                reviewService.getPendingReviews(touristId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Public: browse reviews and rating summary ─────────────────────────

    /**
     * Returns visible reviews for a specific provider entity.
     *
     * <p><b>GET /api/reviews/entity/{entityType}/{entityId}</b>
     *
     * @param entityType one of HOTEL, TOUR_GUIDE, VEHICLE
     * @param entityId   primary-key of the provider
     * @param page       pagination page (0-based, default 0)
     * @param size       page size (default 10)
     * @return paginated list of reviews
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get reviews for an entity",
               description = "Returns all visible reviews for a hotel, tour guide, or vehicle")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReviewDto>>> getReviewsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginatedResponse<ReviewDto> result =
                reviewService.getReviewsByEntity(entityType, entityId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Returns the aggregate rating summary (average, count, histogram) for
     * a provider entity.
     *
     * <p><b>GET /api/reviews/summary/{entityType}/{entityId}</b>
     *
     * @param entityType one of HOTEL, TOUR_GUIDE, VEHICLE
     * @param entityId   primary-key of the provider
     * @return rating summary DTO
     */
    @GetMapping("/summary/{entityType}/{entityId}")
    @Operation(summary = "Get rating summary",
               description = "Returns average rating, total review count, and star distribution for a provider")
    public ResponseEntity<ApiResponse<ReviewSummaryDto>> getReviewSummary(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {

        ReviewSummaryDto summary = reviewService.getReviewSummary(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    /**
     * Fetches a single review by its ID, including provider responses.
     *
     * <p><b>GET /api/reviews/{id}</b>
     *
     * @param reviewId primary-key of the review
     * @return the full review DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a review by ID", description = "Returns a single review with all provider responses")
    public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(
            @Parameter(description = "Review ID") @PathVariable("id") Long reviewId) {

        ReviewDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review));
    }

    // ── Provider: respond to reviews ──────────────────────────────────────

    /**
     * Allows a provider to post a public reply to a tourist's review.
     *
     * <p><b>POST /api/reviews/{id}/responses</b>
     *
     * @param reviewId primary-key of the review
     * @param request  the response text
     * @param auth     authentication token (provider)
     * @return 201 Created with the saved response
     */
    @PostMapping("/{id}/responses")
    @PreAuthorize("hasAnyRole('HOTEL_OWNER', 'TOUR_GUIDE', 'VEHICLE_OWNER', 'ADMIN')")
    @Operation(summary = "Add provider response",
               description = "Provider posts a public reply to a tourist review")
    public ResponseEntity<ApiResponse<ProviderResponseDto>> addProviderResponse(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody ProviderResponseRequest request,
            Authentication auth) {

        String providerId = extractUserId(auth);
        log.info("POST /api/reviews/{}/responses — provider={}", reviewId, providerId);

        ProviderResponseDto response =
                reviewService.addProviderResponse(reviewId, providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Deletes a provider's response.  The response author or an admin may
     * perform this action.
     *
     * <p><b>DELETE /api/reviews/{id}/responses/{responseId}</b>
     *
     * @param reviewId   primary-key of the review
     * @param responseId primary-key of the response
     * @param auth       authentication token
     * @return 204 No Content
     */
    @DeleteMapping("/{id}/responses/{responseId}")
    @PreAuthorize("hasAnyRole('HOTEL_OWNER', 'TOUR_GUIDE', 'VEHICLE_OWNER', 'ADMIN')")
    @Operation(summary = "Delete provider response",
               description = "Provider or admin deletes a response to a review")
    public ResponseEntity<Void> deleteProviderResponse(
            @PathVariable("id") Long reviewId,
            @PathVariable Long responseId,
            Authentication auth) {

        String callerId = extractUserId(auth);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("DELETE /api/reviews/{}/responses/{} — caller={} isAdmin={}",
                reviewId, responseId, callerId, isAdmin);

        reviewService.deleteProviderResponse(reviewId, responseId, callerId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // ── Admin: review moderation ──────────────────────────────────────────

    /**
     * Toggles the visibility of a review (show / hide).
     *
     * <p><b>PATCH /api/reviews/{id}/visibility</b>
     *
     * @param reviewId  primary-key of the review
     * @param isVisible {@code true} to show, {@code false} to hide
     * @return 200 OK with the updated review
     */
    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set review visibility (admin)",
               description = "Admin shows or hides a review without deleting it")
    public ResponseEntity<ApiResponse<ReviewDto>> setReviewVisibility(
            @PathVariable("id") Long reviewId,
            @RequestParam boolean isVisible) {

        log.info("PATCH /api/reviews/{}/visibility?isVisible={} — admin", reviewId, isVisible);

        ReviewDto updated = reviewService.setReviewVisibility(reviewId, isVisible);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Extracts the authenticated user's Supabase user-ID from the
     * Spring Security {@link Authentication} object.
     *
     * <p>The user-ID is set as the authentication principal by
     * {@code JwtValidationFilter} (from common-lib) using the {@code sub}
     * claim of the Supabase JWT.
     *
     * @param auth Spring Security authentication
     * @return Supabase user-ID string
     */
    private String extractUserId(Authentication auth) {
        return (String) auth.getPrincipal();
    }
}
