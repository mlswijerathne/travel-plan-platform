package com.travelplan.booking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Configuration
public class SqsConfig {

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Bean
    public SqsClient sqsClient() {
        try {
            return SqsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        } catch (Exception e) {
            log.warn("AWS SQS client creation failed (expected in local dev): {}", e.getMessage());
            return null;
        }
    }
}
