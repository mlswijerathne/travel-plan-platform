package com.travelplan.review.repository;

import com.travelplan.review.entity.ReviewProviderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ReviewProviderResponse}.
 *
 * <p>A provider can post only one response per review, so most operations
 * are single-result queries.
 */
@Repository
public interface ReviewProviderResponseRepository
        extends JpaRepository<ReviewProviderResponse, Long> {

    /**
     * Finds the response written by a specific provider for a given review.
     * Used to enforce the one-response-per-review-per-provider rule.
     *
     * @param reviewId   primary-key of the parent review
     * @param providerId Supabase Auth user-ID of the provider
     * @return optional response record
     */
    Optional<ReviewProviderResponse> findByReviewIdAndProviderId(
            Long reviewId, String providerId);

    /**
     * Returns all responses for a given review, ordered by creation time.
     * Useful when rendering a review's response list without loading the
     * entire {@link com.travelplan.review.entity.Review} aggregate.
     *
     * @param reviewId primary-key of the parent review
     * @return list of responses
     */
    List<ReviewProviderResponse> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * Checks whether a provider has already responded to a specific review.
     *
     * @param reviewId   primary-key of the review
     * @param providerId Supabase Auth user-ID of the provider
     * @return {@code true} if a response already exists
     */
    boolean existsByReviewIdAndProviderId(Long reviewId, String providerId);
}
