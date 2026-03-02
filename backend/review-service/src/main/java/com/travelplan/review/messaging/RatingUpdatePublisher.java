package com.travelplan.review.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.dto.RatingUpdateEvent;
import com.travelplan.review.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatingUpdatePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String entityType, Long entityId,
                        BigDecimal newRating, int reviewCount) {
        try {
            RatingUpdateEvent event = RatingUpdateEvent.create(
                    entityType, entityId, newRating, reviewCount);

            String messageBody = objectMapper.writeValueAsString(event);
            String key = entityId.toString();

            kafkaTemplate.send(KafkaConfig.TOPIC_RATING_UPDATE_EVENTS, key, messageBody);

            log.info("Published rating update event — {} id={} newRating={} reviewCount={}",
                    entityType, entityId, newRating, reviewCount);

        } catch (Exception e) {
            log.error("Failed to publish rating update event for {} id={}: {}",
                    entityType, entityId, e.getMessage(), e);
        }
    }
}
