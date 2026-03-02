package com.travelplan.itinerary.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("database", "itinerary_db")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withException(e)
                    .build();
        }
    }
}
