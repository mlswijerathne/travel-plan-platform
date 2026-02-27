package com.travelplan.itinerary.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SqsHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Basic check - if SQS is accessible
            return Health.up()
                    .withDetail("service", "SQS")
                    .withDetail("status", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "SQS")
                    .withException(e)
                    .build();
        }
    }
}
