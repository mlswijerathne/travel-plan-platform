package com.travelplan.itinerary.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    // SQS is auto-configured by Spring Cloud AWS, no additional configuration needed
    // The SqsTemplate is automatically available for injection
}

