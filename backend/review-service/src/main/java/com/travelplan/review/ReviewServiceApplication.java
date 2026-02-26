package com.travelplan.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Review Service microservice.
 *
 * <p>{@code @EnableScheduling} activates the SQS polling task defined in
 * {@link com.travelplan.review.messaging.TripCompletionEventListener}.
 *
 * <p>{@code @ComponentScan} ensures that shared beans defined in
 * {@code com.travelplan.common} (JWT filter, CORS config, global exception
 * handler) are picked up alongside the service's own components.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.travelplan.review", "com.travelplan.common"})
public class ReviewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
