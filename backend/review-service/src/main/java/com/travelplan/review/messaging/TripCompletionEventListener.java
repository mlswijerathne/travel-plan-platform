package com.travelplan.review.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.review.dto.TripCompletionEvent;
import com.travelplan.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

/**
 * Scheduled SQS poller that consumes {@code itinerary.trip.completed} events
 * published by the Itinerary Service.
 *
 * <p>For each event, it delegates to
 * {@link ReviewService#processTripCompletionEvent(TripCompletionEvent)}
 * which creates the corresponding {@code PendingReview} records.
 *
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>AWS SDK v2 does not ship a Spring listener annotation, so we use
 *       {@code @Scheduled} for a simple polling loop.</li>
 *   <li>Each message is deleted from the queue <em>only</em> after successful
 *       processing; malformed messages are logged and deleted to prevent
 *       poison-pill queue blocking.</li>
 *   <li>Polling is a no-op when the queue URL is not configured, making local
 *       development straightforward.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TripCompletionEventListener {

    private final SqsClient     sqsClient;
    private final ObjectMapper  objectMapper;
    private final ReviewService reviewService;

    /**
     * SQS queue URL for trip-completion events.
     * Injected from {@code aws.sqs.trip-completion-queue-url} or empty string.
     */
    @Value("${aws.sqs.trip-completion-queue-url:}")
    private String tripCompletionQueueUrl;

    /**
     * Maximum SQS messages per polling request (AWS hard-limit is 10).
     */
    private static final int MAX_MESSAGES = 10;

    /**
     * SQS long-poll wait time in seconds (reduces empty-response noise).
     */
    private static final int WAIT_TIME_SECONDS = 5;

    /**
     * Polls the SQS queue every 10 seconds for trip-completion messages.
     *
     * <p>The {@code fixedDelay} attribute means the next poll starts
     * 10 seconds <em>after</em> the previous one finishes, avoiding
     * overlapping calls if processing is slow.
     *
     * <p>Polling is skipped silently when the queue URL is not set.
     */
    @Scheduled(fixedDelay = 10_000)
    public void pollTripCompletionQueue() {
        if (tripCompletionQueueUrl == null || tripCompletionQueueUrl.isBlank()) {
            // Queue not configured — normal in local development
            return;
        }

        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(tripCompletionQueueUrl)
                    .maxNumberOfMessages(MAX_MESSAGES)
                    .waitTimeSeconds(WAIT_TIME_SECONDS)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            if (!messages.isEmpty()) {
                log.info("Received {} trip-completion message(s) from SQS", messages.size());
            }

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("Error polling trip-completion SQS queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes a single SQS message: deserialises it, delegates to the
     * service layer, then deletes it from the queue.
     *
     * <p>Malformed messages are logged and deleted to avoid queue starvation.
     *
     * @param message raw SQS message
     */
    private void processMessage(Message message) {
        try {
            TripCompletionEvent event =
                    objectMapper.readValue(message.body(), TripCompletionEvent.class);

            log.info("Processing trip-completion event {} for tourist {}",
                    event.getEventId(),
                    event.getPayload() != null ? event.getPayload().getTouristId() : "unknown");

            reviewService.processTripCompletionEvent(event);

            // Delete message only after successful processing (at-least-once delivery)
            deleteMessage(message.receiptHandle());
            log.debug("Message {} deleted from SQS after successful processing",
                    message.messageId());

        } catch (com.fasterxml.jackson.core.JsonProcessingException jsonEx) {
            // Poison-pill: malformed JSON — delete to unblock the queue
            log.error("Malformed trip-completion message {} — deleting: {}",
                    message.messageId(), jsonEx.getMessage());
            deleteMessage(message.receiptHandle());

        } catch (Exception e) {
            // Processing error — do NOT delete so SQS retries (visibility timeout)
            log.error("Error processing trip-completion message {}: {}",
                    message.messageId(), e.getMessage(), e);
        }
    }

    /**
     * Sends a delete request to SQS for a processed message.
     *
     * @param receiptHandle SQS receipt handle from the received message
     */
    private void deleteMessage(String receiptHandle) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(tripCompletionQueueUrl)
                    .receiptHandle(receiptHandle)
                    .build());
        } catch (Exception e) {
            log.error("Failed to delete SQS message: {}", e.getMessage());
        }
    }
}
