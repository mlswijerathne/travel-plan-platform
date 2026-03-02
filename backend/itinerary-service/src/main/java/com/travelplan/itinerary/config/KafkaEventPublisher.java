package com.travelplan.itinerary.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishTripCompletedEvent(String event) {
        log.info("Publishing trip completed event to Kafka topic: {}", KafkaConfig.TOPIC_TRIP_COMPLETION_EVENTS);
        try {
            kafkaTemplate.send(KafkaConfig.TOPIC_TRIP_COMPLETION_EVENTS, event);
            log.info("Trip completed event published successfully");
        } catch (Exception e) {
            log.error("Error publishing trip completed event", e);
        }
    }
}
