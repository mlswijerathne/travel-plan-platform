package com.travelplan.review.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.review.dto.TripCompletionEvent;
import com.travelplan.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripCompletionEventListener {

    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;

    @KafkaListener(topics = "trip-completion-events", groupId = "review-service-group")
    public void handleTripCompletionEvent(String message) {
        try {
            TripCompletionEvent event = objectMapper.readValue(message, TripCompletionEvent.class);

            log.info("Processing trip-completion event {} for tourist {}",
                    event.getEventId(),
                    event.getPayload() != null ? event.getPayload().getTouristId() : "unknown");

            reviewService.processTripCompletionEvent(event);

            log.debug("Trip-completion event {} processed successfully", event.getEventId());

        } catch (Exception e) {
            log.error("Error processing trip-completion event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process trip-completion event", e);
        }
    }
}
