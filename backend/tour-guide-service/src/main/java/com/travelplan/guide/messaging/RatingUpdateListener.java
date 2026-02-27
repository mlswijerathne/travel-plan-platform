package com.travelplan.guide.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.dto.RatingUpdateEvent;
import com.travelplan.guide.repository.GuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdateListener {

    private final GuideRepository guideRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "rating-update-events", groupId = "tour-guide-service-group")
    public void handleRatingUpdate(String message) {
        try {
            RatingUpdateEvent event = objectMapper.readValue(message, RatingUpdateEvent.class);
            log.info("Received rating update event for {} ID: {}",
                    event.getPayload().getEntityType(), event.getPayload().getEntityId());

            if (!"TOUR_GUIDE".equals(event.getPayload().getEntityType())) {
                return;
            }

            guideRepository.findById(event.getPayload().getEntityId()).ifPresent(guide -> {
                guide.setAverageRating(event.getPayload().getNewRating());
                guide.setReviewCount(event.getPayload().getReviewCount());
                guideRepository.save(guide);
                log.info("Updated rating for guide {}: rating={}, count={}",
                        guide.getId(), event.getPayload().getNewRating(), event.getPayload().getReviewCount());
            });
        } catch (Exception e) {
            log.error("Error processing rating update event", e);
            throw new RuntimeException("Failed to process rating update event", e);
        }
    }
}
