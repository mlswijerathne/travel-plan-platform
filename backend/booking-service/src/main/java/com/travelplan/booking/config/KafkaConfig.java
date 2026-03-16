package com.travelplan.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_BOOKING_EVENTS = "booking-events";
    public static final String TOPIC_BOOKING_NOTIFICATIONS = "booking-notifications";
    public static final String TOPIC_TRIP_COMPLETION = "trip-completion-events";

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(TOPIC_BOOKING_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingNotificationsTopic() {
        return TopicBuilder.name(TOPIC_BOOKING_NOTIFICATIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic tripCompletionTopic() {
        return TopicBuilder.name(TOPIC_TRIP_COMPLETION)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
