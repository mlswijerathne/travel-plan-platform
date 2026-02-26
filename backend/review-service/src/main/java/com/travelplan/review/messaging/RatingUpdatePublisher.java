package com.travelplan.review.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.dto.RatingUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;

/**
 * Publishes {@code review.rating.updated} events to the SQS queue so that
 * provider services (Hotel, Tour Guide, Vehicle) can update their
 * denormalized aggregate ratings without polling the Review Service.
 *
 * <p><b>SQS publishing is best-effort:</b> if the queue URL is not
 * configured (e.g. local development without AWS), the event is logged and
 * silently dropped.  The primary review data in PostgreSQL is always the
 * source of truth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdatePublisher {

    private final SqsClient       sqsClient;
    private final ObjectMapper    objectMapper;

    /**
     * SQS queue URL injected from {@code aws.sqs.rating-update-queue-url}.
     * An empty string disables publishing (local dev mode).
     */
    @Value("${aws.sqs.rating-update-queue-url:}")
    private String ratingUpdateQueueUrl;

    /**
     * Serialises a {@link RatingUpdateEvent} and sends it to SQS.
     *
     * @param entityType  string representation of the provider type (e.g. "HOTEL")
     * @param entityId    primary-key of the provider
     * @param newRating   recalculated average rating
     * @param reviewCount current total number of visible reviews
     */
    public void publish(String entityType, Long entityId,
                        BigDecimal newRating, int reviewCount) {
        // Skip publishing when queue URL is not configured
        if (ratingUpdateQueueUrl == null || ratingUpdateQueueUrl.isBlank()) {
            log.debug("SQS queue URL not configured — skipping rating update event for {} id={}",
                    entityType, entityId);
            return;
        }

        try {
            // Build the standard event payload defined in common-lib
            RatingUpdateEvent event = RatingUpdateEvent.create(
                    entityType, entityId, newRating, reviewCount);

            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendRequest = SendMessageRequest.builder()
                    .queueUrl(ratingUpdateQueueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendRequest);

            log.info("Published rating update event — {} id={} newRating={} reviewCount={}",
                    entityType, entityId, newRating, reviewCount);

        } catch (Exception e) {
            // Log and swallow — SQS failure must not break the review operation
            log.error("Failed to publish rating update event for {} id={}: {}",
                    entityType, entityId, e.getMessage(), e);
        }
    }
}
