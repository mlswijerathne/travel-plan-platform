package com.travelplan.itinerary.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPublisher {
    private final SqsTemplate sqsTemplate;

    @Value("${aws.sqs.review-trigger-queue:review-triggers}")
    private String reviewTriggerQueue;

    public void publishTripCompletedEvent(String event) {
        log.info("Publishing trip completed event to SQS queue: {}", reviewTriggerQueue);
        try {
            sqsTemplate.send(reviewTriggerQueue, event);
            log.info("Trip completed event published successfully");
        } catch (Exception e) {
            log.error("Error publishing trip completed event", e);
        }
    }
}
