package com.travelplan.review.mapper;

import com.travelplan.review.dto.*;
import com.travelplan.review.entity.PendingReview;
import com.travelplan.review.entity.Review;
import com.travelplan.review.entity.ReviewProviderResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hand-written mapper that converts between JPA entities and DTOs.
 *
 * <p>Using a plain Spring {@code @Component} instead of MapStruct keeps the
 * mapping logic visible, debuggable, and free of annotation-processor
 * dependency — a pragmatic choice for this service.  MapStruct can be added
 * later if the number of mappings grows significantly.
 */
@Component
public class ReviewMapper {

    // ── Review entity → DTO ───────────────────────────────────────────────

    /**
     * Converts a {@link Review} entity to a {@link ReviewDto}.
     * Provider responses are included in the DTO only when they have been
     * loaded by the repository (i.e., when fetched with {@code JOIN FETCH}).
     *
     * @param review the source entity; must not be {@code null}
     * @return the mapped read-only DTO
     */
    public ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .touristId(review.getTouristId())
                .entityType(review.getEntityType())
                .entityId(review.getEntityId())
                .bookingId(review.getBookingId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                // Convert String[] (PostgreSQL TEXT[]) to List<String>
                .images(review.getImages() != null
                        ? Arrays.asList(review.getImages())
                        : Collections.emptyList())
                .isVerified(review.getIsVerified())
                .isVisible(review.getIsVisible())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                // Responses may be a lazily-loaded proxy; if the collection is
                // accessible we map it, otherwise an empty list is returned
                .responses(mapResponses(review))
                .build();
    }

    /**
     * Converts a list of {@link Review} entities to a list of {@link ReviewDto}.
     *
     * @param reviews source list
     * @return list of mapped DTOs
     */
    public List<ReviewDto> toDtoList(List<Review> reviews) {
        if (reviews == null) return Collections.emptyList();
        return reviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── ReviewProviderResponse entity → DTO ───────────────────────────────

    /**
     * Converts a {@link ReviewProviderResponse} entity to a
     * {@link ProviderResponseDto}.
     *
     * @param response the source entity; must not be {@code null}
     * @return the mapped DTO
     */
    public ProviderResponseDto toResponseDto(ReviewProviderResponse response) {
        return ProviderResponseDto.builder()
                .id(response.getId())
                .reviewId(response.getReview() != null ? response.getReview().getId() : null)
                .providerId(response.getProviderId())
                .content(response.getContent())
                .createdAt(response.getCreatedAt())
                .build();
    }

    // ── PendingReview entity → DTO ────────────────────────────────────────

    /**
     * Converts a {@link PendingReview} entity to a {@link PendingReviewDto}.
     *
     * @param pending the source entity; must not be {@code null}
     * @return the mapped DTO
     */
    public PendingReviewDto toPendingDto(PendingReview pending) {
        return PendingReviewDto.builder()
                .id(pending.getId())
                .touristId(pending.getTouristId())
                .entityType(pending.getEntityType())
                .entityId(pending.getEntityId())
                .entityName(pending.getEntityName())
                .bookingId(pending.getBookingId())
                .tripEndDate(pending.getTripEndDate())
                .isCompleted(pending.getIsCompleted())
                .createdAt(pending.getCreatedAt())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Safely maps the responses collection on a Review.
     * Hibernate lazy-loading may throw an exception if the session is closed;
     * returning an empty list is the safe fallback.
     */
    private List<ProviderResponseDto> mapResponses(Review review) {
        try {
            if (review.getResponses() == null || review.getResponses().isEmpty()) {
                return Collections.emptyList();
            }
            return review.getResponses().stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // LazyInitializationException or similar — responses not loaded
            return Collections.emptyList();
        }
    }
}
